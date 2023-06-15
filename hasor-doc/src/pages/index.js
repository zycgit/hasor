import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import Translate from '@docusaurus/Translate';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import HomepageFeatures from '../components/HomepageFeatures';

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <h1 className="hero__title">{siteConfig.title}</h1>
                <p className="hero__subtitle"><Translate id="homepage.tagline">Hasor 本身是由多个不同系列框架组合而成的一个框架体系。这些子框架的能力涵盖了 IoC、Aop、WebMVC、数据库以及其它方方面面。</Translate>
                    <br/><br/>
                    <a className="button-padding" target="_blank" href="https://www.apache.org/licenses/LICENSE-2.0.html">
                        <img src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg" alt="License"/>
                    </a>
                    <a className="button-padding" target="_blank" href="https://maven-badges.herokuapp.com/maven-central/net.hasor/hasor-dataql">
                        <img src="https://maven-badges.herokuapp.com/maven-central/net.hasor/hasor-dataql/badge.svg" alt="Maven"/>
                    </a>
                    <a className="button-padding" target="_blank" href="mailto:zyc@byshell.org">
                        <img src="https://img.shields.io/badge/Email-zyc%40byshell.org-blue" alt="mailto"/>
                    </a>
                    <a className="button-padding" target="_blank" href="https://qm.qq.com/cgi-bin/qm/qr?k=o4Ue0lHqdr7oLq8ga0vvauXuw41nudbo&jump_from=webapi">
                        <img border="0" src="https://img.shields.io/badge/QQ%E7%BE%A41-193943114-orange" alt="dbVisitor ORM 交流群1" title="DataQL 交流群1"/>
                    </a>
                    <a className="button-padding" target="_blank" href="https://qm.qq.com/cgi-bin/qm/qr?k=wMahYnxpVZPjrJp0ghQQLJmwM2Lmpmjl&jump_from=webapi">
                        <img border="0" src="https://img.shields.io/badge/QQ%E7%BE%A42-641341864-orange" alt="dbVisitor ORM 交流群2" title="DataQL 交流群2"/>
                    </a>
                </p>
                <div className={styles.buttons}>
                    <Link className="button button--secondary button--lg" to="/docs/guides/quickstart"><Translate id="homepage.dataql">快速上手</Translate></Link>
                </div>
            </div>
        </header>
    );
}

export default function Home() {
    return (
        <Layout>
            <HomepageHeader/>
            <main>
                <HomepageFeatures/>
            </main>
        </Layout>
    );
}
