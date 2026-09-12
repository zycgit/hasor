// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const prismReactRenderer = require('prism-react-renderer');
const lightCodeTheme = prismReactRenderer.themes ? prismReactRenderer.themes.github : require('prism-react-renderer/themes/github');
const darkCodeTheme = prismReactRenderer.themes ? prismReactRenderer.themes.dracula : require('prism-react-renderer/themes/dracula');
const analyticsPlugin = require('./plugins/analytics.js');
const projectVars = require('./plugins/projectVars.js');

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'Hasor',
    tagline: 'Hasor 框架',
    url: 'http://www.hasor.net',
    baseUrl: '/',
    onBrokenLinks: 'throw',
    markdown: {
        preprocessor: ({fileContent}) => require('./plugins/remark-project-vars.js').replaceVariables(fileContent, projectVars),
        // Resolve variables before extracting metadata or recognizing Markdown links.
        parseFrontMatter: ({filePath, fileContent, defaultParseFrontMatter}) => defaultParseFrontMatter({
            filePath,
            fileContent: require('./plugins/remark-project-vars.js').replaceVariables(fileContent, projectVars),
        }),
        hooks: {
            onBrokenMarkdownLinks: 'warn',
        },
    },
    favicon: 'img/favicon.ico',
    organizationName: 'zycgit', // Usually your GitHub org/user name.
    projectName: 'hasor',   // Usually your repo name.

    i18n: {
        defaultLocale: 'zh-cn',
        locales: ['zh-cn', 'en'],
    },

    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    remarkPlugins: [[require('./plugins/remark-project-vars.js'), projectVars]],
                    sidebarPath: require.resolve('./sidebars.js'),
                    editUrl: 'https://gitee.com/zycgit/hasor-doc/tree/master/',
                },
                blog: false,
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }),
        ],
    ],

    themeConfig: /** @type {import('@docusaurus/preset-classic').ThemeConfig} */ {
        metadata: [
            {
                name: 'keywords',
                content: 'hasor,hasor-core,hasor-config,hasor-web,hasor-boot,ioc,aop,webmvc,java开源,开源项目',
            },
            {
                name: 'description',
                content: 'Hasor 是一个轻量级 Java 框架，当前核心能力由 hasor-core、hasor-config、hasor-web、hasor-boot 组成。',
            }
        ],
        colorMode: {
            disableSwitch: true,
        },
        navbar: {
            logo: {
                alt: 'Hasor Logo',
                src: 'img/logo.svg',
            },
            items: [
                {
                    type: 'doc',
                    docId: 'guides/getting-started/quickstart',
                    position: 'left',
                    label: '文档手册',
                },
                {
                    type: 'doc',
                    docId: 'releases/latest',
                    position: 'left',
                    label: '版本说明',
                },
                {
                    label: '码云',
                    href: 'https://gitee.com/zycgit/hasor',
                    position: 'right',
                },
                {
                    label: 'Github',
                    href: 'https://github.com/zycgit/hasor',
                    position: 'right',
                },
                {
                    type: 'localeDropdown',
                    position: 'right',
                }
            ]
        },
        prism: {
            theme: lightCodeTheme,
            darkTheme: darkCodeTheme,
            additionalLanguages: ['java']
        },
        footer: {
            style: 'dark',
            copyright: `Copyright © ${new Date().getFullYear()} Hasor. Built with Docusaurus.<br/>
<a target="_blank" href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=33011002013536">
<img src="/img/beian.png" style="display: inline-block;">浙公网安备 33011002013536号
</a>&nbsp;&nbsp;<a target="_blank" href="https://beian.miit.gov.cn/#/Integrated/index">浙ICP备18034797号-1</a>
<div id="analyticsDiv" style="display: inline-block;"></div>`,
        },
    },
    plugins: [
        analyticsPlugin
    ],
    themes: [
        [
            require.resolve("@easyops-cn/docusaurus-search-local"),
            /** @type {import("@easyops-cn/docusaurus-search-local").PluginOptions} */
            ({
                hashed: true,
                indexBlog: false,
                language: ["en", "zh"],
            }),
        ],
    ]
};

module.exports = function createConfig() {
    if (process.env.DOCUSAURUS_CURRENT_LOCALE !== 'en') {
        return config;
    }
    const messages = require('./i18n/en/code.json');
    return {
        ...config,
        tagline: messages['site.tagline'].message,
        themeConfig: {
            ...config.themeConfig,
            metadata: config.themeConfig.metadata.map((entry) => ({
                ...entry,
                content: messages['site.' + entry.name]?.message ?? entry.content,
            })),
        },
    };
};
