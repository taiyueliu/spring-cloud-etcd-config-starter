package com.nyquistdata.config.parser;

import org.springframework.core.io.ByteArrayResource;

/**
 * @author Nyquist Data Tech Team
 * @version 1.0.0
 * @date 2022/5/6
 * @description ETCD-specific resource.
 */
public class EtcdByteArrayResource extends ByteArrayResource {
    private String filename;
    public EtcdByteArrayResource(byte[] byteArray) {
        super(byteArray);
    }
    public EtcdByteArrayResource(byte[] byteArray, String description) {
        super(byteArray, description);
    }
    public void setFilename(String filename) {
        this.filename = filename;
    }
    @Override
    public String getFilename() {
        return null == this.filename ? this.getDescription() : this.filename;
    }
}
