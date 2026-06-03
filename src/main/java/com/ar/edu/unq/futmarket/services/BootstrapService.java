package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.controllers.response.ApiGeneralResponse;
import com.ar.edu.unq.futmarket.controllers.response.BootstrapResponse;

public interface BootstrapService {

    BootstrapResponse initializeDemoData();
    ApiGeneralResponse removeAllData();
}
