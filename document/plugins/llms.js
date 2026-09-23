/*
 * Copyright 2015-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
const {loadFreshModule} = require('@docusaurus/utils');
const {readFile, writeFile} = require('node:fs/promises');
const path = require('node:path');
const projectVars = require('./projectVars.js');
const {replaceVariables} = require('./remark-project-vars.js');

module.exports = async function llmsPlugin(context, {overview, descriptions, ...options}) {
    const createPlugin = await loadFreshModule(require.resolve('@signalwire/docusaurus-plugin-llms-txt'));
    const {siteConfig, i18n} = context;
    const plugin = createPlugin(context, {
        ...options,
        siteDescription: descriptions[i18n.currentLocale],
        enableDescriptions: false,
        // Preserve upstream route paths, then resolve absolute URLs in the index below.
        content: {...options.content, relativePaths: true},
    });
    return {
        ...plugin,
        async postBuild(props) {
            await plugin.postBuild(props);
            const {unified} = await import('unified');
            const {default: remarkParse} = await import('remark-parse');
            const {default: remarkGfm} = await import('remark-gfm');
            const {default: remarkStringify} = await import('remark-stringify');
            const markdown = unified().use(remarkParse).use(remarkGfm, {tablePipeAlign: false})
                .use(remarkStringify, {bullet: '-'});
            const file = path.join(props.outDir, 'llms.txt');
            const tree = markdown.parse(await readFile(file, 'utf8'));
            const groups = {docs: new Map(), drivers: new Map(), features: new Map(), releases: new Map(), blog: new Map()};
            const base = new URL(siteConfig.baseUrl, siteConfig.url);
            function collect(node) {
                if (node.type === 'link') {
                    const url = new URL(node.url, base);
                    const relative = url.pathname.slice(base.pathname.length);
                    if (url.origin !== base.origin || !url.pathname.startsWith(base.pathname)) {
                        throw new Error('Unexpected documentation index URL: ' + node.url);
                    }
                    node.url = url.href;
                    let group = 'docs';
                    if (relative.startsWith('docs/releases/')) {
                        group = 'releases';
                    } else if (relative.startsWith('docs/drivers/')) {
                        group = 'drivers';
                    } else if (relative.startsWith('docs/features/')) {
                        group = 'features';
                    } else if (relative.startsWith('blog/')) {
                        group = 'blog';
                    }
                    groups[group].set(node.url, node);
                }
                for (const child of node.children ?? []) {
                    collect(child);
                }
            }
            collect(tree);
            const english = i18n.currentLocale === 'en';
            const titles = english ? {
                docs: 'Usage guides', drivers: 'JDBC drivers', features: 'Data source capabilities',
                releases: 'Release notes', blog: 'Articles and scenarios',
            } : {docs: '使用指南', drivers: 'JDBC 驱动', features: '数据源能力', releases: '版本说明', blog: '博客与场景文章'};
            const summary = await readOverview(context, overview, markdown, base);
            const document = {type: 'root', children: [
                {type: 'heading', depth: 1, children: [{type: 'text', value: options.siteTitle}]},
                ...summary.children,
                {type: 'paragraph', children: [{type: 'text', value:
                    (english ? 'Guide version: ' : '使用指南版本：') + projectVars.docsVersion +
                    (english ? '. Match the project dependency version to the relevant guides and release notes.' :
                        '。使用时先核对项目依赖版本，再参考对应指南与版本说明。')}]},
            ]};
            let optionalSection = false;
            for (const [group, links] of Object.entries(groups)) {
                if (links.size === 0) {
                    continue;
                }
                const optional = group === 'releases' || group === 'blog';
                if (optional && !optionalSection) {
                    document.children.push({type: 'heading', depth: 2, children: [{type: 'text', value: 'Optional'}]});
                    optionalSection = true;
                }
                const items = [...links.values()].map(link => ({type: 'listItem', spread: false,
                    children: [{type: 'paragraph', children: [link]}]}));
                document.children.push(
                    {type: 'heading', depth: optional ? 3 : 2, children: [{type: 'text', value: titles[group]}]},
                    {type: 'list', ordered: false, spread: false, children: items},
                );
            }
            await writeFile(file, markdown.stringify(document));
        },
        injectHtmlTags() {
            return {
                headTags: [{
                    tagName: 'link',
                    attributes: {
                        rel: 'describedby',
                        type: 'text/plain',
                        href: siteConfig.baseUrl + 'llms.txt',
                    },
                }],
            };
        },
    };
};

async function readOverview({siteDir, siteConfig, i18n}, overview, markdown, base) {
    const directory = i18n.currentLocale === i18n.defaultLocale ? 'docs' :
        'i18n/' + i18n.currentLocale + '/docusaurus-plugin-content-docs/current';
    const file = path.join(siteDir, directory, overview);
    const source = replaceVariables(await readFile(file, 'utf8'), projectVars);
    const sections = [...source.matchAll(/\{\/\* llms:start \*\/\}([\s\S]*?)\{\/\* llms:end \*\/\}/g)];
    if (sections.length !== 1) {
        throw new Error('Expected one llms summary in ' + file);
    }
    const tree = markdown.parse(sections[0][1]);
    function resolveLinks(node) {
        if (['link', 'definition'].includes(node.type) && !/^(?:[a-z]+:|\/\/|#)/i.test(node.url)) {
            if (node.url.startsWith('/')) {
                node.url = new URL(node.url, siteConfig.url).href;
            } else {
                const target = new URL(node.url, 'https://docs.invalid/' + overview);
                const documentPath = target.pathname.slice(1).replace(/\.(md|mdx)$/, '');
                node.url = new URL('docs/' + documentPath, base).href + target.search + target.hash;
            }
        }
        for (const child of node.children ?? []) {
            resolveLinks(child);
        }
    }
    resolveLinks(tree);
    return tree;
}
