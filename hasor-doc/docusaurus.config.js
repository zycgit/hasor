// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');
const analyticsPlugin = require('./plugins/analytics.js');

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'Hasor',
    tagline: 'Hasor 框架',
    url: 'http://www.hasor.net',
    baseUrl: '/',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',
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
                    sidebarPath: require.resolve('./sidebars.js'),
                    editUrl: 'https://gitee.com/zycgit/hasor-doc/tree/master/',
                },
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }),
        ],
    ],

    themeConfig: /** @type {import('@docusaurus/preset-classic').ThemeConfig} */ {
        metadata: [
            {name: 'keywords', content: 'sql,dataway,hasor,dataql,开源,开源软件,java开源,开源项目,开源代码'},
            {name: 'description', content: 'Hasor 本身是由多个不同系列框架组合而成的一个框架体系。这些子框架的能力涵盖了 IoC、Aop、WebMVC、数据库以及其它方方面面。'}
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
                    docId: 'guides/quickstart',
                    position: 'left',
                    label: '文档手册',
                },
                {
                    type: 'doc',
                    docId: 'integration/overview',
                    position: 'left',
                    label: '框架集成',
                },
                {
                    type: 'dropdown',
                    label: '源代码',
                    position: 'left',
                    items: [
                        {label: '码云',href: 'https://gitee.com/zycgit/hasor'},
                        {label: 'Github',href: 'https://github.com/zycgit/hasor'}
                    ]
                },
                {
                    position: 'right',
                    label: 'DataQL 语言',
                    href: 'https://www.dataql.net/'
                },
                {
                    position: 'right',
                    label: 'Dataway',
                    href: 'https://www.dataql.net/'
                },
                {
                    position: 'right',
                    label: 'dbVisitor ORM',
                    href: 'https://www.dbvisitor.net/'
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
            copyright: `Copyright © ${new Date().getFullYear()} dbVisitor. Built with Docusaurus.<br/>
<a target="_blank" href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=33011002013536">
<img src="/img/beian.png" style="display: inline-block;">浙公网安备 33011002013536号
</a>&nbsp;&nbsp;<a target="_blank" href="https://beian.miit.gov.cn/#/Integrated/index">浙ICP备18034797号-1</a>
<div id="analyticsDiv" style="display: inline-block;"></div>`,
        },
    },
    plugins: [
        analyticsPlugin,
        [
            require.resolve("@cmfcmf/docusaurus-search-local"),
            {
                indexPages: true,
                // When applying `zh` in language, please install `nodejieba` in your project.
                language: ["en", "zh"],
                maxSearchResults: 8
            }
        ]
    ]
};

module.exports = config;
