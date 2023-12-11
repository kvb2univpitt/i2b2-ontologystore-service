/*
 * Copyright (C) 2023 University of Pittsburgh.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package edu.pitt.dbmi.i2b2.ontologystore;

import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummaryType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyDownloadService;
import edu.pitt.dbmi.i2b2.ontologystore.service.OntologyFileService;
import edu.pitt.dbmi.i2b2.ontologystore.utils.StringUtils;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * Dec 5, 2023 5:56:39 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@SpringBootTest
public class OntologyStoreServiceApplicationTests {

    private final OntologyFileService ontologyFileService;
    private final OntologyDownloadService ontologyDownloadService;

    @Autowired
    public OntologyStoreServiceApplicationTests(
            OntologyFileService ontologyFileService,
            OntologyDownloadService ontologyDownloadService) {
        this.ontologyFileService = ontologyFileService;
        this.ontologyDownloadService = ontologyDownloadService;
    }

    @Test
    public void contextLoads() {
        System.out.println("================================================================================");
        testOntologyFileService();
        System.out.println("================================================================================");
    }

    private void testOntologyDownloadService() {
        ProductActionType actMedVaV4 = new ProductActionType();
        actMedVaV4.setId("act_med_va_v4");
        actMedVaV4.setDownload(true);

        ProductActionType actVitalSingsV4 = new ProductActionType();
        actVitalSingsV4.setId("act_vital_signs_v4");
        actVitalSingsV4.setDownload(true);

        List<ProductActionType> actions = new LinkedList<>();
        actions.add(actMedVaV4);
        actions.add(actVitalSingsV4);

        List<ActionSummaryType> summaries = new LinkedList<>();

        ontologyDownloadService.performDownload(actions, summaries);
        summaries.stream()
                .map(StringUtils::toString)
                .forEach(System.out::println);
    }

    private void testOntologyFileService() {
        ontologyFileService.getProductsForDisplay().stream()
                .map(StringUtils::toString)
                .forEach(System.out::println);
    }

}