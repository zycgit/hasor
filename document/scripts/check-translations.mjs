import assert from 'node:assert/strict';
import { createHash } from 'node:crypto';
import { existsSync, readFileSync, readdirSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const documentRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const sourceRoot = path.join(documentRoot, 'docs');
const englishRoot = path.join(documentRoot, 'i18n/en/docusaurus-plugin-content-docs/current');
const hash = (text) => createHash('sha256').update(text).digest('hex');
const han = /\p{Script=Han}/u;

export function checkPage(relative, source, english) {
  assert(!han.test(english), `Untranslated Chinese text: ${relative}`);
  for (const key of ['id', 'sidebar_position']) {
    const value = (text) => text.match(new RegExp('^' + key + ': (.+)$', 'm'))?.[1];
    assert.equal(value(english), value(source), `Metadata mismatch (${key}): ${relative}`);
  }
  const fences = (text) => [...text.matchAll(/^```/gm)].length;
  assert.equal(fences(english), fences(source), `Code block count mismatch: ${relative}`);
  if (/^releases\/[0-5]\.x\/v/.test(relative)) {
    const dates = (text) => [...text.matchAll(/\b\d{4}-\d{2}-\d{2}\b/g)].map((m) => m[0]);
    assert.deepEqual(dates(english), dates(source), `Release date mismatch: ${relative}`);
    const headings = (text) => [...text.matchAll(/^(#{1,6}) /gm)].map((m) => m[1]);
    assert.deepEqual(headings(english), headings(source), `Release section mismatch: ${relative}`);
    const bullets = (text) => [...text.matchAll(/^( *)- /gm)].map((m) => m[1].length);
    assert.deepEqual(bullets(english), bullets(source), `Release item mismatch: ${relative}`);
  }
}

function files(root) {
  return readdirSync(root, { withFileTypes: true }).flatMap((entry) => {
    const target = path.join(root, entry.name);
    return entry.isDirectory() ? files(target) : /\.(md|mdx|json)$/.test(entry.name) ? [target] : [];
  });
}

export function checkTranslations() {
  const manifest = JSON.parse(readFileSync(path.join(documentRoot, 'i18n/translation-status.json'), 'utf8'));
  const sourceFiles = files(sourceRoot).map((f) => path.relative(sourceRoot, f)).sort();
  const englishFiles = files(englishRoot).map((f) => path.relative(englishRoot, f)).sort();
  assert.deepEqual(englishFiles, sourceFiles, 'Missing or obsolete English documents');
  assert.deepEqual(Object.keys(manifest.documents).sort(), sourceFiles, 'Translation inventory is incomplete');
  const sidebar = JSON.parse(readFileSync(path.join(documentRoot, 'i18n/en/docusaurus-plugin-content-docs/current.json'), 'utf8'));
  for (const relative of sourceFiles) {
    const source = readFileSync(path.join(sourceRoot, relative), 'utf8');
    const english = readFileSync(path.join(englishRoot, relative), 'utf8');
    const record = manifest.documents[relative];
    assert.equal(hash(source), record.sourceSha256, `Chinese source changed; review translation: ${relative}`);
    assert.equal(hash(english), record.englishSha256, `English translation changed; review parity: ${relative}`);
    if (relative.endsWith('.json')) {
      const structural = (text) => JSON.parse(text, (key, value) => ['label', 'title', 'description'].includes(key) ? undefined : value);
      assert.deepEqual(structural(english), structural(source), `Category structure mismatch: ${relative}`);
      assert(!han.test(english), `Untranslated category: ${relative}`);
      const category = JSON.parse(source);
      const key = `sidebar.${relative.split('/')[0]}.category.${category.key ?? category.label}`;
      assert.equal(sidebar[key]?.message, JSON.parse(english).label, `Missing sidebar translation: ${relative}`);
      continue;
    }
    checkPage(relative, source, english);
    for (const [, target] of english.matchAll(/\]\(([^\s)]+)(?:\s+[^)]*)?\)/g)) {
      if (/^(?:[a-z]+:|\/|#)/i.test(target)) continue;
      const destination = target.split('#')[0];
      assert(existsSync(path.resolve(englishRoot, path.dirname(relative), decodeURI(destination))), `Broken relative link: ${relative} → ${target}`);
    }
  }
  for (const [relative, expected] of Object.entries(manifest.siteFiles)) {
    assert.equal(hash(readFileSync(path.join(documentRoot, relative), 'utf8')), expected, `Site translation needs review: ${relative}`);
  }
  const navbar = JSON.parse(readFileSync(path.join(documentRoot, 'i18n/en/docusaurus-theme-classic/navbar.json'), 'utf8'));
  assert.equal(navbar['item.label.版本说明']?.message, 'Release Notes');
  console.log(`Translation checks passed: ${sourceFiles.filter((p) => p.startsWith('guides/') && /\.mdx?$/.test(p)).length} guides, ${sourceFiles.filter((p) => p.startsWith('releases/') && /\.mdx?$/.test(p)).length} release pages, and ${sourceFiles.filter((p) => p.endsWith('.json')).length} category files.`);
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) checkTranslations();
