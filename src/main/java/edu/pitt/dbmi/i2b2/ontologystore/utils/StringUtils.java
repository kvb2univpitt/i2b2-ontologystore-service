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
package edu.pitt.dbmi.i2b2.ontologystore.utils;

import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ActionSummaryType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductActionType;
import edu.pitt.dbmi.i2b2.ontologystore.datavo.vdo.ProductType;
import edu.pitt.dbmi.i2b2.ontologystore.model.PackageFile;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * Dec 7, 2023 3:27:46 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public final class StringUtils {

    private StringUtils() {
    }

    public static String toString(PackageFile packageFile) {
        StringBuilder sb = new StringBuilder();
        sb.append("PackageFile{");
        sb.append("\n   tableAccess=").append(Arrays.stream(packageFile.getTableAccess()).collect(Collectors.joining(",")));
        sb.append(",\n    schemes=").append(packageFile.getSchemes());
        sb.append(",\n    breakdownPath=").append(packageFile.getBreakdownPath());
        sb.append(",\n    adapterMapping=").append(packageFile.getAdapterMapping());
        sb.append(",\n    shrineIndex=").append(packageFile.getShrineIndex());
        sb.append(",\n    conceptDimensions=").append(Arrays.stream(packageFile.getConceptDimensions()).collect(Collectors.joining(",")));
        sb.append(",\n    domainOntologies=").append(Arrays.stream(packageFile.getDomainOntologies()).collect(Collectors.joining(",")));
        sb.append("\n}");

        return sb.toString();
    }

    public static String toString(ActionSummaryType type) {
        StringBuilder sb = new StringBuilder();
        sb.append("ActionSummaryType{");
        sb.append("\n    title=").append(type.getTitle());
        sb.append(",\n    actionType=").append(type.getActionType());
        sb.append(",\n    inProgress=").append(type.isInProgress());
        sb.append(",\n    success=").append(type.isSuccess());
        sb.append(",\n    detail=").append(type.getDetail());
        sb.append("\n}");

        return sb.toString();
    }

    public static String toString(ProductActionType type) {
        StringBuilder sb = new StringBuilder();
        sb.append("ProductActionType{");
        sb.append("\n    id=").append(type.getId());
        sb.append(",\n    download=").append(type.isDownload());
        sb.append(",\n    install=").append(type.isInstall());
        sb.append(",\n    disable=").append(type.isDisableEnable());
        sb.append("\n}");

        return sb.toString();
    }

    public static String toString(ProductType type) {
        StringBuilder sb = new StringBuilder();
        sb.append("ProductType{");
        sb.append("\n    id=").append(type.getId());
        sb.append(",\n    title=").append(type.getTitle());
        sb.append(",\n    version=").append(type.getVersion());
        sb.append(",\n    owner=").append(type.getOwner());
        sb.append(",\n    type=").append(type.getType());
        sb.append(",\n    terminologies=").append(type.getTerminologies().getTerminology());
        sb.append(",\n    includeNetworkPackage=").append(type.isIncludeNetworkPackage());
        sb.append(",\n    downloaded=").append(type.isDownloaded());
        sb.append(",\n    installed=").append(type.isInstalled());
        sb.append(",\n    started=").append(type.isStarted());
        sb.append(",\n    failed=").append(type.isFailed());
        sb.append(",\n    disabled=").append(type.isDisabled());
        sb.append(",\n    statusDetail=").append(type.getStatusDetail());
        sb.append("\n}");

        return sb.toString();
    }

}
