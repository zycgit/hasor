// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');
const {translate} = require('@docusaurus/Translate');
const analyticsPlugin = require('./plugins/analytics.js');

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'Hasor',
    tagline: translate({
        id: 'site.tagline',
        message: 'Hasor 框架',
        description: 'The site tagline',
    }),
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
            {
                name: 'keywords',
                content: translate({
                    id: 'site.keywords',
                    message: 'hasor,hasor-core,hasor-web,hasor-boot,ioc,aop,webmvc,java开源,开源项目',
                    description: 'The site keywords',
                }),
            },
            {
                name: 'description',
                content: translate({
                    id: 'site.description',
                    message: 'Hasor 是一个轻量级 Java 框架，当前核心能力由 hasor-core、hasor-web、hasor-boot 组成。',
                    description: 'The site description',
                }),
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
                    docId: 'guides/quickstart',
                    position: 'left',
                    label: translate({
                        id: 'navbar.docs',
                        message: '文档手册',
                        description: 'The navbar documentation link',
                    }),
                },
                {
                    type: 'doc',
                    docId: 'integration/overview',
                    position: 'left',
                    label: translate({
                        id: 'navbar.integration',
                        message: '框架集成',
                        description: 'The navbar integration link',
                    }),
                },
                {
                    type: 'dropdown',
                    label: translate({
                        id: 'navbar.source',
                        message: '源代码',
                        description: 'The navbar source code dropdown',
                    }),
                    position: 'left',
                    items: [
                        {
                            label: translate({
                                id: 'navbar.gitee',
                                message: '码云',
                                description: 'The Gitee link label',
                            }),
                            href: 'https://gitee.com/zycgit/hasor'
                        },
                        {label: 'Github',href: 'https://github.com/zycgit/hasor'}
                    ]
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
