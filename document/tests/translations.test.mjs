import assert from 'node:assert/strict';
import test from 'node:test';
import { createRequire } from 'node:module';
import { checkPage, checkTitles } from '../scripts/check-translations.mjs';

const source = '---\nid: v1.0.0\nsidebar_position: 1\n---\n# v1.0.0 (2020-01-01)\n\n## 更新内容\n\n- 新增\n  - 示例。\n';
const english = source.replace('更新内容', 'Changes').replace('新增', 'Added').replace('示例。', 'Example.');
const release = 'releases/1.x/v1.0.0.md';

test('matching page and sidebar titles pass, ignoring code headings', () => {
  assert.doesNotThrow(() => checkTitles('example.md', '---\ntitle: Example\n---\n# Example\n\n```sh\n# A shell comment\n```'));
});
test('mismatched titles, multiple H1s and sidebar overrides fail', () => {
  for (const body of ['# Different', '# Example\n# Another', '# Example\nsidebar_label: Different']) {
    assert.throws(() => checkTitles('example.md', 'title: Example\n' + body), /mismatch/);
  }
});

test('navbar groups content on the left and repository links on the right', () => {
  const items = createRequire(import.meta.url)('../docusaurus.config.js')().themeConfig.navbar.items;
  assert.deepEqual(items.filter((item) => item.position === 'left').map((item) => item.label),
    ['文档手册', '版本说明']);
  const right = items.filter((item) => item.position === 'right');
  assert.deepEqual(right.map((item) => item.label ?? item.type), ['码云', 'Github', 'localeDropdown']);
  assert.equal(right[0].href, 'https://gitee.com/zycgit/hasor');
  assert.equal(right[1].href, 'https://github.com/zycgit/hasor');
  assert(!items.some((item) => item.type === 'dropdown'));
});

test('matching translation passes', () => assert.doesNotThrow(() => checkPage(release, source, english)));
test('Chinese leftovers fail', () => assert.throws(() => checkPage(release, source, source), /Untranslated/));
test('changed release dates fail', () => assert.throws(() => checkPage(release, source, english.replace('2020-01-01', '2020-02-02')), /date mismatch/));
test('missing release items fail', () => assert.throws(() => checkPage(release, source, english.replace('  - Example.\n', '')), /item mismatch/));
test('changed document IDs fail', () => assert.throws(() => checkPage(release, source, english.replace('id: v1.0.0', 'id: v2.0.0')), /Metadata mismatch/));
test('missing code examples fail', () => assert.throws(() => checkPage('guides/example.md', '```java\nrun();\n```', ''), /Code block count/));

test('site metadata follows each locale without mutating Chinese config', () => {
  const createConfig = createRequire(import.meta.url)('../docusaurus.config.js');
  const previous = process.env.DOCUSAURUS_CURRENT_LOCALE;
  try {
    process.env.DOCUSAURUS_CURRENT_LOCALE = 'zh-cn';
    const chinese = createConfig();
    process.env.DOCUSAURUS_CURRENT_LOCALE = 'en';
    const english = createConfig();
    assert.equal(english.tagline, 'Hasor Framework');
    assert(english.themeConfig.metadata.every((entry) => !/\p{Script=Han}/u.test(entry.content)));
    assert.equal(chinese.tagline, 'Hasor 框架');
    process.env.DOCUSAURUS_CURRENT_LOCALE = 'zh-cn';
    assert.equal(createConfig().tagline, chinese.tagline);
  } finally {
    if (previous === undefined) delete process.env.DOCUSAURUS_CURRENT_LOCALE;
    else process.env.DOCUSAURUS_CURRENT_LOCALE = previous;
  }
});
