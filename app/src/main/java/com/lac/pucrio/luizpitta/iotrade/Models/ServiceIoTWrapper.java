package com.lac.pucrio.luizpitta.iotrade.Models;


import java.io.Serializable;

public class ServiceIoTWrapper implements Serializable {
    private static final long serialVersionUID = 4L;
    private ServiceIoT serviceIoT;

    public ServiceIoTWrapper(ServiceIoT serviceIoT) {
        this.serviceIoT = serviceIoT;
    }

    public ServiceIoT getServiceIoT() {
        return serviceIoT;
    }

}