import React from 'react';
import clsx from 'clsx';
import styles from './HomepageFeatures.module.css';
import Translate, {translate} from '@docusaurus/Translate';

const FeatureList = [
    {
        title: translate({id: 'homepage.feature1_title', message: '体系完整'}),
        Svg: require('../../static/img/undraw_docusaurus_mountain.svg').default,
        description: (
            <><Translate id="homepage.feature1_desc">IoC、Aop、WebMVC等</Translate></>
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
            <><Translate id="homepage.feature6_desc">独有的 API 融合机制会，让框架新的能力完全无缝的集成到统一的 API 体系中</Translate></>
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
