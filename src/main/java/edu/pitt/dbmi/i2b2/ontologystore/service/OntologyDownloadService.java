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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
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

    @Autowired
    public OntologyDownloadService(FileSysService fileSysService, OntologyFileService ontologyFileService, DataSource ontologydemodsDataSource, DataSource querytooldemodsDataSource) {
        super(fileSysService, ontologyFileService, ontologydemodsDataSource, querytooldemodsDataSource);
    }

    public synchronized void performDownload(List<ProductActionType> actions, List<ActionSummaryType> summaries) {
        // get actions that are marked for download
        actions = actions.stream().filter(e -> e.isDownload()).collect(Collectors.toList());

        List<ProductItem> productsToDownload = getValidProductsToDownload(actions, summaries);
        productsToDownload = downloadFiles(productsToDownload, summaries);
        verifyFileIntegrity(productsToDownload, summaries);
    }

    /**
     * Verify the integrity of the downloaded products by computing the SHA-256
     * checksum and compare it with the ones given in the product list.
     *
     * @param productsToDownload
     * @param summaries
     */
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

    /**
     * Download production from the given product list.
     *
     * @param productsToDownload a list of products to download
     * @param summaries a list to store download summaries
     * @return a list products that are successfully downloaded
     */
    private List<ProductItem> downloadFiles(List<ProductItem> productsToDownload, List<ActionSummaryType> summaries) {
        List<ProductItem> downloadedProducts = new LinkedList<>();

        productsToDownload.forEach(productItem -> {
            String productFolder = productItem.getId();
            Path productDir = fileSysService.getProductDirectory(productFolder);
            if (fileSysService.createDirectory(productDir) && fileSysService.createDownloadStartedIndicatorFile(productFolder)) {
                try {
                    // download product file
                    fileSysService.downloadFile(productItem.getFile(), productDir);

                    //download network files, if any
                    String[] networkFiles = productItem.getNetworkFiles();
                    if (hasNetworkFiles(networkFiles)) {
                        Path networkDir = Paths.get(productDir.toString(), "network_files");
                        if (fileSysService.createDirectory(networkDir)) {
                            try {
                                for (String networkFile : networkFiles) {
                                    fileSysService.downloadFile(networkFile, networkDir);
                                }
                            } catch (IOException exception) {
                                LOGGER.error("", exception);
                            }
                        } else {
                            summaries.add(createActionSummary(productItem.getTitle(), ACTION_TYPE, false, false, "Unable to download adapter mapping files."));
                        }
                    }
                    downloadedProducts.add(productItem);
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

        return downloadedProducts;
    }

    private boolean hasNetworkFiles(String[] networkFiles) {
        return !(networkFiles == null || networkFiles.length == 0);
    }

    /**
     * Get a list of product items based on the download-action list that meet
     * the following conditions:
     *
     * <ul>
     * <li>Products that have not been downloaded.</li>
     * <li>Products that have not previous failed to download.</li>
     * <li>Products that are currently been downloaded.</li>
     * </ul>
     *
     * @param actions a list of download actions
     * @param summaries a list to store download summaries
     * @return a list of products to download
     */
    private List<ProductItem> getValidProductsToDownload(List<ProductActionType> actions, List<ActionSummaryType> summaries) {
        List<ProductItem> validProductItems = new LinkedList<>();

        Map<String, ProductItem> products = ontologyFileService.getProductItems();
        actions.forEach(action -> {
            String productFolder = action.getId();
            if (products.containsKey(productFolder)) {
                ProductItem productItem = products.get(productFolder);
                if (fileSysService.hasDirectory(productFolder)) {
                    if (fileSysService.hasFinshedDownload(productFolder) && fileSysService.isProductFileExists(productItem)) {
                        summaries.add(createActionSummary(productItem.getTitle(), ACTION_TYPE, false, true, "Already downloaded."));
                        return;
                    } else if (fileSysService.hasFailedDownload(productFolder)) {
                        summaries.add(createActionSummary(productItem.getTitle(), ACTION_TYPE, false, false, fileSysService.getFailedDownloadMessage(productFolder)));
                        return;
                    } else if (fileSysService.hasStartedDownload(productFolder)) {
                        summaries.add(createActionSummary(productItem.getTitle(), ACTION_TYPE, true, false, "Download already started."));
                        return;
                    }
                }

                if (!action.isIncludeNetworkPackage()) {
                    productItem.setNetworkFiles(new String[0]);
                }

                validProductItems.add(productItem);
            }
        });

        return validProductItems;
    }

}
