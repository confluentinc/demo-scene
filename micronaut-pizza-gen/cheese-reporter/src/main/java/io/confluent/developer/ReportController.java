package io.confluent.developer;

import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.*;
import jakarta.inject.Inject;
import io.micronaut.views.View;

@Controller("/report")
public class ReportController {
    @Inject
    private CheeseReport cheeseReport;

    @View("report")
    @Get("/")
    public HttpResponse getReport(){
        return HttpResponse.ok(CollectionUtils.mapOf("cheeses", cheeseReport.getReport()));
    }

}
