package com.ar.edu.unq.futmarket.services;

public interface BootstrapService {

    BootstrapResult initializeDemoData();

    void removeAllData();
}
