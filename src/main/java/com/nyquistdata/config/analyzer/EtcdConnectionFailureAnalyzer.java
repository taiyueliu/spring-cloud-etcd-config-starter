package com.nyquistdata.config.analyzer;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/6
 * @description todo
 */
public class EtcdConnectionFailureAnalyzer extends AbstractFailureAnalyzer<EtcdConnectionFailureException> {
    @Override
    protected FailureAnalysis analyze(Throwable rootFailure,
                                      EtcdConnectionFailureException cause) {
        return new FailureAnalysis("Application failed to connect to etcd server: \""
                + cause.getServerAddr() + "\"",
                "Please check your etcd server config", cause);
    }
}
