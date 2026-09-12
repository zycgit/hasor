import assert from 'node:assert/strict';
import test from 'node:test';
import { createRequire } from 'node:module';
import { compile } from '@mdx-js/mdx';

const require = createRequire(import.meta.url);
const plugin = require('../plugins/remark-project-vars.js');
const variables = require('../plugins/projectVars.js');

test('text, inline code, code blocks and links share the configured version', () => {
  const nodes = ['text', 'inlineCode', 'code'].map((type) => ({type, value: 'net.hasor:hasor-core:@project.docsVersion@'}));
  nodes.push({type: 'link', url: '/@project.docsVersion@/', children: []});
  plugin(variables)({type: 'root', children: nodes});
  for (const node of nodes.slice(0, 3)) assert.equal(node.value, `net.hasor:hasor-core:${variables.docsVersion}`);
  assert.equal(nodes[3].url, `/${variables.docsVersion}/`);
});

test('unknown variables fail instead of leaking placeholders', () => {
  assert.throws(() => plugin(variables)({type: 'text', value: '@project.docVersion@'}), /Unknown or invalid/);
});

test('fixed historical versions are unchanged', () => {
  const node = {type: 'text', value: 'v5.1.0 (2026-09-08)'};
  plugin(variables)(node);
  assert.equal(node.value, 'v5.1.0 (2026-09-08)');
});

test('real MDX compilation resolves versions in text and fenced examples', async () => {
  const source = '# Hasor @project.docsVersion@\n\n`@project.docsVersion@`\n\n```xml\n<version>@project.docsVersion@</version>\n```';
  for (const version of ['5.2.0', '5.3.0']) {
    const output = String(await compile(source, {remarkPlugins: [[plugin, {...variables, docsVersion: version}]]}));
    assert(output.includes(`<version>${version}</version>`));
    assert(output.includes(`Hasor ${version}`));
    assert(!output.includes('@project.'));
  }
});

test('site configuration passes variables into the Markdown compiler', () => {
  const config = require('../docusaurus.config.js')();
  const [configuredPlugin, options] = config.presets[0][1].docs.remarkPlugins[0];
  assert.equal(configuredPlugin, plugin);
  assert.deepEqual(options, variables);
});

test('metadata extraction and MDX parsing resolve variables before autolinking', async () => {
  const {parseMarkdownFile} = require('@docusaurus/utils');
  const config = require('../docusaurus.config.js')();
  const parsed = await parseMarkdownFile({
    filePath: 'latest.mdx',
    fileContent: '# Release Notes\n\nLatest version: **v@project.lastReleaseVer@**\n\n```xml\n<version>@project.docsVersion@</version>\n```',
    parseFrontMatter: config.markdown.parseFrontMatter,
  });
  assert(!parsed.excerpt.includes('@project.'));
  assert(parsed.excerpt.includes(`v${variables.lastReleaseVer}`));
  // The MDX loader uses the original source, not the content returned by parseFrontMatter.
  const output = String(await compile(config.markdown.preprocessor({
    fileContent: '# Release Notes\n\nLatest version: **v@project.lastReleaseVer@**\n\n```xml\n<version>@project.docsVersion@</version>\n```',
  })));
  assert(!output.includes('mailto:'));
  assert(!output.includes('@project.'));
  assert(output.includes(`<version>${variables.docsVersion}</version>`));
});
