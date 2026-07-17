import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';
import Translate, {translate} from '@docusaurus/Translate';

const FeatureList = [
    {
        title: translate({id: 'homepage.feature1_title', message: '核心清晰'}),
        Svg: require('../../static/img/undraw_docusaurus_mountain.svg').default,
        description: (
            <><Translate id="homepage.feature1_desc">hasor-core、hasor-web、hasor-boot 三个核心模块覆盖容器、Web 和可执行包。</Translate></>
        ),
    },
    {
        title: translate({id: 'homepage.feature2_title', message: '微内核+插件'}),
        Svg: require('../../static/img/undraw_docusaurus_tree.svg').default,
        description: (
            <><Translate id="homepage.feature2_desc">提供少量必要的功能支持、其余功能全部通过插件化方式实现</Translate></>
        ),
    },
    {
        title: translate({id: 'homepage.feature6_title', message: '统一API'}),
        Svg: require('../../static/img/undraw_docusaurus_react.svg').default,
        description: (
            <><Translate id="homepage.feature6_desc">普通应用使用 ApiBinder，Web 应用使用 WebApiBinder，在同一套容器模型下扩展能力。</Translate></>
        ),
    },
];

function Feature({Svg, title, description}) {
    return (
        <div className={clsx('col col--4')}>
            <div className="text--center">
                <Svg className={styles.featureSvg} alt={title}/>
            </div>
            <div className="text--center padding-horiz--md">
                <h3>{title}</h3>
                <p>{description}</p>
            </div>
        </div>
    );
}

export default function HomepageFeatures() {
    return (
        <section className={styles.features}>
            <div className="container">
                <div className="row">
                    {FeatureList.map((props, idx) => (
                        <Feature key={idx} {...props} />
                    ))}
                </div>
            </div>
        </section>
    );
}
