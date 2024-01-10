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
import edu.pitt.dbmi.i2b2.ontologystore.utils.ZipFileValidation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private final String downloadDirectory;

    @Autowired
    public OntologyStoreServiceApplicationTests(
            OntologyFileService ontologyFileService,
            OntologyDownloadService ontologyDownloadService,
            @Value("${ontology.dir.download}") String downloadDirectory) {
        this.downloadDirectory = downloadDirectory;
        this.ontologyFileService = ontologyFileService;
        this.ontologyDownloadService = ontologyDownloadService;
    }

    @Test
    public void contextLoads() {
        System.out.println("================================================================================");
//        testGetAvailableProducts();
//        testOntologyDownloadService();
//        testZipFileValidation();
        System.out.println("================================================================================");
    }

    private void testZipFileValidation() {
        Path zipFilePath = Paths.get(downloadDirectory, "act_network_ontology_v4/act_network_ontology_v4.zip");
        ZipFileValidation zipFileValidation = new ZipFileValidation(zipFilePath);
        try {
            zipFileValidation.validate();
        } catch (ZipFileValidationException exception) {
            System.err.println(exception.getMessage());
        }
    }

    private void testOntologyDownloadService() {
        String[] ontologies = {
            //            "act_covid_v4",
            //            "act_cpt4_px_v4",
            //            "act_hcpcs_px_v4",
            //            "act_icd10cm_dx_v4",
            //            "act_icd10_icd9_dx_v4",
            //            "act_icd10pcs_px_v4",
            //            "act_icd9cm_dx_v4",
            //            "act_icd9cm_px_v4",
            //            "act_loinc_lab_prov_v4",
            //            "act_loinc_lab_v4",
            //            "act_med_alpha_v4",
            //            "act_med_va_v4",
            "act_network_ontology_v4",
            //            "act_sdoh_v4",
            //            "act_visit_details_v4",
            "act_vital_signs_v4"
        };

        List<ProductActionType> actions = new LinkedList<>();
        for (String ontology : ontologies) {
            ProductActionType ont = new ProductActionType();
            ont.setId(ontology);
            ont.setDownload(true);
            actions.add(ont);
        }

        List<ActionSummaryType> summaries = new LinkedList<>();

        ontologyDownloadService.performDownload(actions, summaries);
        summaries.stream()
                .map(StringUtils::toString)
                .forEach(System.out::println);
    }

    private void testGetAvailableProducts() {
        ontologyFileService.getAvailableProducts().stream()
                .map(StringUtils::toString)
                .forEach(System.out::println);
    }

}
