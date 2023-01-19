package com.volodya262.jbproductsinfo.application.services

import com.volodya262.jbproductsinfo.application.clients.JetBrainsDataServicesClient
import com.volodya262.jbproductsinfo.application.clients.JetBrainsUpdatesClient
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class MainService(
    val jetBrainsDataServicesClient: JetBrainsDataServicesClient,
    val jetBrainsUpdatesClient: JetBrainsUpdatesClient,
    val buildsProviderService: BuildsProviderService
) {
   fun doStuff() {
       val todayMinusYear = LocalDate.now().minusYears(1)
       val (products, second) = buildsProviderService.getProductsBuilds()

       println(products)
   }
}