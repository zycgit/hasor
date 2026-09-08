import React from 'react';
import clsx from 'clsx';
import Layout from '@theme/Layout';
import Link from '@docusaurus/Link';
import Translate, {translate} from '@docusaurus/Translate';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import styles from './index.module.css';
import HomepageFeatures from '../components/HomepageFeatures';

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className={clsx('hero hero--primary', styles.heroBanner)}>
            <div className="container">
                <h1 className="hero__title">{siteConfig.title}</h1>
                <p className="hero__subtitle"><Translate id="homepage.tagline">Hasor 是一个轻量级 Java 框架，当前核心能力由 hasor-core、hasor-config、hasor-web、hasor-boot 组成。</Translate>
                    <br/><br/>
                    <a className="button-padding" target="_blank" href="https://www.apache.org/licenses/LICENSE-2.0.html">
                        <img src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg" alt="License"/>
                    </a>
                    <a className="button-padding" target="_blank" href="mailto:zyc@byshell.org">
                        <img src="https://img.shields.io/badge/Email-zyc%40byshell.org-blue" alt="mailto"/>
                    </a>
                    <a className="button-padding" target="_blank" href="https://qm.qq.com/cgi-bin/qm/qr?k=o4Ue0lHqdr7oLq8ga0vvauXuw41nudbo&jump_from=webapi">
                        <img border="0" src={translate({
                            id: 'homepage.qqGroup1Badge',
                            message: 'https://img.shields.io/badge/QQ%E7%BE%A41-193943114-orange',
                            description: 'The QQ group 1 badge URL',
                        })} alt={translate({
                            id: 'homepage.qqGroup1Alt',
                            message: 'Hasor 交流群1',
                            description: 'The QQ group 1 image alt text',
                        })} title={translate({
                            id: 'homepage.qqGroup1Title',
                            message: 'Hasor 交流群1',
                            description: 'The QQ group 1 image title',
                        })}/>
                    </a>
                    <a className="button-padding" target="_blank" href="https://qm.qq.com/cgi-bin/qm/qr?k=wMahYnxpVZPjrJp0ghQQLJmwM2Lmpmjl&jump_from=webapi">
                        <img border="0" src={translate({
                            id: 'homepage.qqGroup2Badge',
                            message: 'https://img.shields.io/badge/QQ%E7%BE%A42-641341864-orange',
                            description: 'The QQ group 2 badge URL',
                        })} alt={translate({
                            id: 'homepage.qqGroup2Alt',
                            message: 'Hasor 交流群2',
                            description: 'The QQ group 2 image alt text',
                        })} title={translate({
                            id: 'homepage.qqGroup2Title',
                            message: 'Hasor 交流群2',
                            description: 'The QQ group 2 image title',
                        })}/>
                    </a>
                </p>
                <div className={styles.buttons}>
                    <Link className="button button--secondary button--lg" to="/docs/guides/getting-started/quickstart"><Translate id="homepage.quickstart">快速上手</Translate></Link>
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
