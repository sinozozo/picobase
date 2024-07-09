import {createRequire} from 'module'
import {type DefaultTheme, defineConfig} from 'vitepress'

const require = createRequire(import.meta.url)
const pkg = require('vitepress/package.json')

export const zh = defineConfig({
    lang: 'zh-Hans',
    description: 'An open source low-code framework for building web applications.',

    themeConfig: {
        nav: nav(),

        sidebar: {
            '/guide/': {base: '/guide/', items: sidebarGuide()},
            '/reference/': {base: '/reference/', items: sidebarReference()}
        },

        editLink: {
            pattern: 'https://github.com/sinozozo/picobase/tree/main/docs/:path',
            text: '在 GitHub 上编辑此页面'
        },

        /*footer: {
            message: '基于 MIT 许可发布',
            copyright: `版权所有 © 2019-${new Date().getFullYear()} zozo`
        },*/
        footer: {
            message: '',
            copyright: ` © 2019-${new Date().getFullYear()} zozo`
        },

        docFooter: {
            prev: '上一页',
            next: '下一页'
        },

        outline: {
            label: '页面导航'
        },

        lastUpdated: {
            text: '最后更新于',
            formatOptions: {
                dateStyle: 'short',
                timeStyle: 'medium'
            }
        },

        langMenuLabel: '多语言',
        returnToTopLabel: '回到顶部',
        sidebarMenuLabel: '菜单',
        darkModeSwitchLabel: '主题',
        lightModeSwitchTitle: '切换到浅色模式',
        darkModeSwitchTitle: '切换到深色模式'
    }
})

function nav(): DefaultTheme.NavItem[] {
    return [
        {
            text: '指南',
            link: '/guide/what-is-picobase',
            activeMatch: '/guide/'
        },
        /*{
            text: '参考',
            link: '/reference/site-config',
            activeMatch: '/reference/'
        },*/
        {
            text: '加入讨论群',
            link: '/join-discussion',
            activeMatch: '/join-discussion'
        },
        {
            text: pkg.version,
            items: [
                {
                    text: '更新日志',
                    link: '/guide/changelog'
                },
                {
                    text: '参与贡献',
                    link: '/guide/contributing'
                }
            ]
        }
    ]
}

function sidebarGuide(): DefaultTheme.SidebarItem[] {
    return [
        {
            text: '简介',
            collapsed: false,
            items: [
                {text: '什么是 PicoBase？', link: 'what-is-picobase'},
                {text: '快速开始', link: 'getting-started'}
            ]
        },
        {
            text: '基础',
            collapsed: false,
            items: [
                {text: 'Collection', link: 'collection'},
                {text: 'API访问规则和过滤', link: 'api-rules-and-filters'},
                {text: '用户认证', link: 'authentication'},
                {text: '实体关系', link: 'model-relation'},
                {text: '文件上传和处理', link: 'file-upload'}
            ]
        },
        {
            text: '作为框架使用',
            collapsed: false,
            items: [
                {text: '总体介绍', link: 'frame-intro'},
                {text: '用户认证', link: 'user-login'},
                {text: 'EventBus', link: 'eventbus'},
                {text: 'Scheduler', link: 'scheduler'},
                {text: '数据库操作', link: 'db-operation'},
            ]
        },
        {
            text: '写作',
            collapsed: false,
            items: [
                {text: 'Markdown 扩展', link: 'markdown'},
                {text: '资源处理', link: 'asset-handling'},
                {text: 'frontmatter', link: 'frontmatter'},
                {text: '在 Markdown 使用 Vue', link: 'using-vue'},
                {text: '国际化', link: 'i18n'}
            ]
        },
        {
            text: '自定义',
            collapsed: false,
            items: [
                {text: '自定义主题', link: 'custom-theme'},
                {text: '扩展默认主题', link: 'extending-default-theme'},
                {text: '构建时数据加载', link: 'data-loading'},
                {text: 'SSR 兼容性', link: 'ssr-compat'},
                {text: '连接 CMS', link: 'cms'}
            ]
        },
        {
            text: '实验性功能',
            collapsed: false,
            items: [
                {text: 'MPA 模式', link: 'mpa-mode'},
                {text: 'sitemap 生成', link: 'sitemap-generation'}
            ]
        },
        {text: '配置和 API 参考', base: '/reference/', link: 'site-config'}
    ]
}

function sidebarReference(): DefaultTheme.SidebarItem[] {
    return [
        {
            text: '参考',
            items: [
                {text: '站点配置', link: 'site-config'},
                {text: 'frontmatter 配置', link: 'frontmatter-config'},
                {text: '运行时 API', link: 'runtime-api'},
                {text: 'CLI', link: 'cli'},
                {
                    text: '默认主题',
                    base: '/reference/default-theme-',
                    items: [
                        {text: '概览', link: 'config'},
                        {text: '导航栏', link: 'nav'},
                        {text: '侧边栏', link: 'sidebar'},
                        {text: '主页', link: 'home-page'},
                        {text: '页脚', link: 'footer'},
                        {text: '布局', link: 'layout'},
                        {text: '徽章', link: 'badge'},
                        {text: '团队页', link: 'team-page'},
                        {text: '上下页链接', link: 'prev-next-links'},
                        {text: '编辑链接', link: 'edit-link'},
                        {text: '最后更新时间戳', link: 'last-updated'},
                        {text: '搜索', link: 'search'},
                        {text: 'Carbon Ads', link: 'carbon-ads'}
                    ]
                }
            ]
        }
    ]
}

export const search: DefaultTheme.AlgoliaSearchOptions['locales'] = {
    zh: {
        placeholder: '搜索文档',
        translations: {
            button: {
                buttonText: '搜索文档',
                buttonAriaLabel: '搜索文档'
            },
            modal: {
                searchBox: {
                    resetButtonTitle: '清除查询条件',
                    resetButtonAriaLabel: '清除查询条件',
                    cancelButtonText: '取消',
                    cancelButtonAriaLabel: '取消'
                },
                startScreen: {
                    recentSearchesTitle: '搜索历史',
                    noRecentSearchesText: '没有搜索历史',
                    saveRecentSearchButtonTitle: '保存至搜索历史',
                    removeRecentSearchButtonTitle: '从搜索历史中移除',
                    favoriteSearchesTitle: '收藏',
                    removeFavoriteSearchButtonTitle: '从收藏中移除'
                },
                errorScreen: {
                    titleText: '无法获取结果',
                    helpText: '你可能需要检查你的网络连接'
                },
                footer: {
                    selectText: '选择',
                    navigateText: '切换',
                    closeText: '关闭',
                    searchByText: '搜索提供者'
                },
                noResultsScreen: {
                    noResultsText: '无法找到相关结果',
                    suggestedQueryText: '你可以尝试查询',
                    reportMissingResultsText: '你认为该查询应该有结果？',
                    reportMissingResultsLinkText: '点击反馈'
                }
            }
        }
    }
}
