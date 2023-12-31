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
package edu.pitt.dbmi.i2b2.ontologystore.service;

import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummaryType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import edu.pitt.dbmi.i2b2.ontologystore.model.ProductItem;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Dec 6, 2023 7:02:12 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
@Service
public class OntologyDownloadService extends AbstractOntologyService {

    private static final Log LOGGER = LogFactory.getLog(OntologyDownloadService.class);

    private static final String ACTION_TYPE = "Download";

    private final FileSysService fileSysService;
    private final OntologyFileService ontologyFileService;

    @Autowired
    public OntologyDownloadService(FileSysService fileSysService, OntologyFileService ontologyFileService) {
        this.fileSysService = fileSysService;
        this.ontologyFileService = ontologyFileService;
    }

    public synchronized void performDownload(List<ProductActionType> actions, List<ActionSummaryType> summaries) {
        // get all download actions
        actions = actions.stream().filter(e -> e.isDownload()).collect(Collectors.toList());

        List<ProductItem> productsToDownload = getValidProductsToDownload(actions, summaries);
        productsToDownload = downloadFiles(productsToDownload, summaries);
        verifyFileIntegrity(productsToDownload, summaries);
    }

    private void verifyFileIntegrity(List<ProductItem> productsToDownload, List<ActionSummaryType> summaries) {
        productsToDownload.forEach(productItem -> {
            String productFolder = productItem.getId();

            String fileURI = productItem.getFile();
            Path productDir = fileSysService.getProductDirectory(productFolder);
            String generatedSha256Checksum = fileSysService.getSha256Checksum(fileURI, productDir);
            if (generatedSha256Checksum.compareTo(productItem.getSha256Checksum()) == 0) {
                fileSysService.createDownloadFinishedIndicatorFile(productFolder);
                summaries.add(createActionSummary(productItem.getTitle(), ACTION_TYPE, false, true, "Downloaded successfully."));
            } else {
                String errorMsg = "File verification failed.  SHA-256 checksum does not match.";
                fileSysService.createDownloadFailedIndicatorFile(productFolder, errorMsg);
                summaries.add(createActionSummary(productItem.getTitle(), ACTION_TYPE, false, false, errorMsg));
            }
        });
    }

    private List<ProductItem> downloadFiles(List<ProductItem> productsToDownload, List<ActionSummaryType> summaries) {
        List<ProductItem> validProductItems = new LinkedList<>();

        productsToDownload.forEach(productItem -> {
            String productFolder = productItem.getId();
            Path productDir = fileSysService.getProductDirectory(productFolder);
            if (fileSysService.createDirectory(productDir)
                    && fileSysService.createDownloadStartedIndicatorFile(productFolder)) {
                try {
                    fileSysService.downloadFile(productItem.getFile(), productDir);
                    validProductItems.add(productItem);
                } catch (Exception exception) {
                    LOGGER.error("", exception);
                    String errorMsg = "Unable to download from the given URL.";
                    fileSysService.createDownloadFailedIndicatorFile(productFolder, errorMsg);
                    summaries.add(createActionSummary(productItem.getTitle(), ACTION_TYPE, false, false, errorMsg));
                }
            } else {
                summaries.add(createActionSummary(productItem.getTitle(), ACTION_TYPE, false, false, "Unable to create directories for download."));
            }
        });

        return validProductItems;
    }

    private List<ProductItem> getValidProductsToDownload(List<ProductActionType> actions, List<ActionSummaryType> summaries) {
        List<ProductItem> validProductItems = new LinkedList<>();

        Map<String, ProductItem> products = ontologyFileService.getProductItems();
        actions.forEach(action -> {
            String productFolder = action.getId();
            if (products.containsKey(productFolder)) {
                ProductItem productItem = products.get(productFolder);
                if (fileSysService.hasDirectory(productFolder)) {
                    if (fileSysService.hasFinshedDownload(productFolder)) {
                        summaries.add(createActionSummary(productItem.getTitle(), ACTION_TYPE, false, true, "Already downloaded."));
                    } else if (fileSysService.hasFailedDownload(productFolder)) {
                        summaries.add(createActionSummary(productItem.getTitle(), ACTION_TYPE, false, false, fileSysService.getFailedDownloadMessage(productFolder)));
                    } else if (fileSysService.hasStartedDownload(productFolder)) {
                        summaries.add(createActionSummary(productItem.getTitle(), ACTION_TYPE, true, false, "Download already started."));
                    } else {
                        validProductItems.add(productItem);
                    }
                } else {
                    validProductItems.add(productItem);
                }
            }
        });

        return validProductItems;
    }

}
