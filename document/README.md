
## 构建项目

在仓库根目录执行 `./build.sh package test`，或在本目录执行 `../gradlew clean build`。应用 Java 编译目标为 17。

## 构建文档网站

本目录使用 Docusaurus，Node.js 要求为 20 或以上版本。已有依赖清单包含搜索插件，无需单独安装 nodejieba。

```bash
npm ci
npm run build
```

`npm run start` 启动本地预览。侧边栏自动读取 `docs/guides`，新增页面通过 front matter 的 `sidebar_position` 设置顺序。构建涵盖中文和英文 locale；英文没有对应翻译的页面使用默认文档。
