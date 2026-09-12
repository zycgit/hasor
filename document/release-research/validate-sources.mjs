import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import { readFileSync, readdirSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const research = path.dirname(fileURLToPath(import.meta.url));
const releases = path.resolve(research, '../docs/releases');
const readJson = (name) => JSON.parse(readFileSync(path.join(research, name), 'utf8'));
const database = readJson('project-version-raw.json').records;
const articles = readJson('oschina-raw.json').articles;
const merged = readJson('merged-core-web.json');
const alignment = readJson('tag-alignment.json');
const maven = readJson('maven-release-dates.json');
const mavenDates = new Map(maven.records.map((r) => [r.version, r.timestamp.slice(0, 10)]));
assert.equal(mavenDates.size, 67);
assert.equal(maven.timezone, 'UTC');
const repository = path.resolve(research, '../..');
const gitReleases = readJson('git-release-details.json');
assert.equal(gitReleases.repository, repository);
assert.deepEqual(gitReleases.records.map((r) => r.version), ['4.2.4', '4.2.5', '5.0.0', '5.0.1', '5.1.0']);
for (const record of gitReleases.records) {
  const metadata = execFileSync('git', ['show', '-s', '--format=%H%n%aI%n%cI', record.tag + '^{commit}'], {
    cwd: repository, encoding: 'utf8',
  }).trim().split('\n');
  assert.deepEqual(metadata, [record.commit, record.authorDate, record.committerDate]);
  assert.equal(record.displayDate, record.authorDate.slice(0, 10));
  const text = readFileSync(path.join(releases, record.version[0] + '.x/v' + record.version + '.md'), 'utf8');
  assert(text.includes('title: v' + record.version + ' (' + record.displayDate + ')\n'));
  assert(text.includes('# v' + record.version + ' (' + record.displayDate + ')\n'));
  assert(text.includes(record.releaseContent), 'Git 推导内容与发布页不一致：' + record.version);
  assert(!/历史记录与核对依据|<details>|<summary>/.test(text));
}
const tags = execFileSync('git', ['tag', '--list', 'Release.Hasor-*'], {
  cwd: repository, encoding: 'utf8',
}).trim().split('\n').sort();
assert.deepEqual([...alignment.tags].sort(), tags, '标签清单与当前仓库不一致');
assert.equal(alignment.repository, repository);
const targets = new Map(alignment.merges.map((r) => [r.sourcePage, r.targetPage]));
assert.equal(targets.size, alignment.merges.length);
const expectedPages = new Set(tags.map((tag) => {
  const version = tag.replace('Release.Hasor-', '');
  return `${version[0]}.x/v${version}.md`;
}));
for (const page of alignment.developmentPages) expectedPages.add(page);
for (const entry of alignment.merges) {
  assert(tags.includes(entry.targetTag));
  const targetVersion = entry.targetTag.replace('Release.Hasor-', '');
  assert.equal(entry.targetPage, `${targetVersion[0]}.x/v${targetVersion}.md`);
  assert(expectedPages.has(entry.targetPage));
  assert(!expectedPages.has(entry.sourcePage));
  assert(entry.originalMarkdown.includes('## 更新内容'), '被归并页面没有备份');
  if (!entry.sourcePage.includes('web-') && !entry.sourcePage.includes('M1')) {
    const compare = (a, b) => {
      const left = a.split('.').map(Number);
      const right = b.split('.').map(Number);
      return left[0] - right[0] || left[1] - right[1] || left[2] - right[2];
    };
    const sourceVersion = path.basename(entry.sourcePage, '.md').slice(1);
    const nextTag = tags.map((tag) => tag.replace('Release.Hasor-', ''))
      .filter((version) => compare(version, sourceVersion) > 0).sort(compare)[0];
    assert.equal(targetVersion, nextTag, '中间版本应归入后续最近的标签');
  }
}
assert.equal(database.length, 42);
assert.equal(articles.length, 44);
assert.equal(new Set(database.map((r) => r.id)).size, database.length);
assert.equal(new Set(articles.map((r) => r.id)).size, articles.length);
const articleIds = new Set(articles.map((r) => r.id));
const covered = new Set(merged.excludedArticles.map((r) => r.id));
const latest = readFileSync(path.join(releases, 'latest.mdx'), 'utf8');
const pageKeys = new Set();

for (const record of merged.records) {
  const name = `${record.module === 'hasor-web' ? 'web-' : ''}v${record.version}`;
  const sourcePage = `${record.version[0]}.x/${name}.md`;
  const relative = targets.get(sourcePage) || sourcePage;
  pageKeys.add(relative);
  const text = readFileSync(path.join(releases, relative), 'utf8');
  const targetId = path.basename(relative, '.md');
  assert(text.includes(`id: ${targetId}\n`), `页面 ID 不匹配：${relative}`);
  assert(text.includes('## 影响范围\n\n') && text.includes('## 更新内容\n\n'));
  assert(latest.includes(`./${relative}`), `版本入口缺少链接：${relative}`);
  assert(!/历史记录与核对依据|<details>|<summary>/.test(text));
  assert(!/DataQL|Dataway|MyBatis|JFinal|RSF|tConsole|JdbcTemplate|事务/.test(text), `范围外条目：${relative}`);
  for (const group of ['new', 'opt', 'fix', 'upgrade']) {
    for (const item of record[group] || []) assert(text.includes(item), `页面缺少条目：${relative} ${item}`);
  }
  const displayedDate = targets.has(sourcePage) ? record.releaseDate : (mavenDates.get(record.version) || record.releaseDate);
  if (displayedDate) assert(text.includes(`(${displayedDate})`));
  if (record.databaseId) {
    const source = database.find((r) => r.id === record.databaseId);
    assert.equal(source.version, record.version);
    assert.equal(source.release_time.slice(0, 10), record.releaseDate);
  }
  for (const id of record.oschinaIds) {
    assert(articleIds.has(id), `不存在的资讯来源：${id}`);
    covered.add(id);
  }
}
assert.equal(merged.records.filter((r) => r.databaseId).length, 42);
assert.equal(covered.size, articles.length, '有资讯未分类');
const actualPages = new Set();
for (const dir of readdirSync(releases).filter((p) => p.endsWith('.x'))) {
  const positions = new Set();
  for (const file of readdirSync(path.join(releases, dir)).filter((p) => p.endsWith('.md'))) {
    const text = readFileSync(path.join(releases, dir, file), 'utf8');
    const position = text.match(/sidebar_position: (\d+)/)?.[1];
    assert(position && !positions.has(position), `版本排序重复：${dir}/${file}`);
    positions.add(position);
    actualPages.add(`${dir}/${file}`);
  }
}
assert.deepEqual([...actualPages].sort(), [...expectedPages].sort(), '发布页必须与标签及开发版一一对应');
for (const page of actualPages) assert(latest.includes(`./${page}`), `版本入口缺少链接：${page}`);
for (const [version, date] of mavenDates) {
  const page = `${version[0]}.x/v${version}.md`;
  assert(expectedPages.has(page));
  const text = readFileSync(path.join(releases, page), 'utf8');
  assert(text.includes(`title: v${version} (${date})\n`), `制品日期不一致：${page}`);
  assert(text.includes(`# v${version} (${date})\n`), `正文日期不一致：${page}`);
}
for (const source of targets.keys()) assert(!latest.includes(`./${source}`), `残留旧链接：${source}`);
console.log(`校验通过：${tags.length} 个标签 + ${alignment.developmentPages.length} 个开发版；${alignment.merges.length} 个无标签页面已归并，${database.length} 条数据库记录和 ${articles.length} 篇资讯仍完整对应。`);
