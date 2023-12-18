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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.pitt.dbmi.i2b2.ontologystore.ZipFileValidationException;
import edu.pitt.dbmi.i2b2.ontologystore.model.PackageFile;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * Dec 14, 2023 9:33:05 PM
 *
 * @author Kevin V. Bui (kvb2univpitt@gmail.com)
 */
public class ZipFileValidation {

    private static final Log LOGGER = LogFactory.getLog(ZipFileValidation.class);

    private final Path zipFilePath;
    private final String zipFileName;

    public ZipFileValidation(Path zipFilePath) {
        this.zipFilePath = zipFilePath;
        this.zipFileName = zipFilePath.getFileName().toString();
    }

    public void validate() throws ZipFileValidationException {
        if (!Files.exists(zipFilePath)) {
            throw new ZipFileValidationException(String.format("File does not exist: %s.", zipFileName));
        }

        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
            Map<String, ZipEntry> zipEntries = getZipEntries(zipFile);

            ZipEntry packageJsonZipEntry = zipEntries.get("package.json");
            if (packageJsonZipEntry == null) {
                throw new ZipFileValidationException(String.format("Missing package.json file: %s.", zipFileName));
            }

            PackageFile packageFile = getPackageFile(packageJsonZipEntry, zipFile);

            String rootFolder = new File(packageJsonZipEntry.getName()).getParent();
            validateTableAccess(packageFile, rootFolder, zipEntries);
            validateConceptDimension(packageFile, rootFolder, zipEntries);
            validateDomainOntology(packageFile, rootFolder, zipEntries);
            validateScheme(packageFile, rootFolder, zipEntries);
            validateBreakdownPath(packageFile, rootFolder, zipEntries);
        } catch (IOException exception) {
            throw new ZipFileValidationException(String.format("Not a zip file: %s.", zipFileName));
        }
    }

    private void validateBreakdownPath(PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries) throws ZipFileValidationException {
        String file = packageFile.getBreakdownPath();
        if (file == null) {
            throw new ZipFileValidationException(
                    String.format("Breakdown path file missing: %s.", zipFileName));
        }

        String zipFile = Paths.get(rootFolder, file).toString();
        if (!zipEntries.containsKey(zipFile)) {
            throw new ZipFileValidationException(
                    String.format("Missing file: File %s not found in %s.", zipFile, zipFileName));
        }
    }

    private void validateScheme(PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries) throws ZipFileValidationException {
        String file = packageFile.getSchemes();
        if (file == null) {
            throw new ZipFileValidationException(
                    String.format("Schemes file missing: %s.", zipFileName));
        }

        String zipFile = Paths.get(rootFolder, file).toString();
        if (!zipEntries.containsKey(zipFile)) {
            throw new ZipFileValidationException(
                    String.format("Missing file: File %s not found in %s.", zipFile, zipFileName));
        }
    }

    private void validateDomainOntology(PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries) throws ZipFileValidationException {
        String[] tableAccess = packageFile.getDomainOntologies();
        if (tableAccess == null || tableAccess.length == 0) {
            throw new ZipFileValidationException(
                    String.format("Domain ontology files missing: %s.", zipFileName));
        }

        for (String file : tableAccess) {
            String zipFile = Paths.get(rootFolder, file).toString();
            if (!zipEntries.containsKey(zipFile)) {
                throw new ZipFileValidationException(
                        String.format("Missing file: File %s not found in %s.", zipFile, zipFileName));
            }
        }
    }

    private void validateConceptDimension(PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries) throws ZipFileValidationException {
        String[] tableAccess = packageFile.getConceptDimensions();
        if (tableAccess == null || tableAccess.length == 0) {
            throw new ZipFileValidationException(
                    String.format("Concept dimension files missing: %s.", zipFileName));
        }

        for (String file : tableAccess) {
            String zipFile = Paths.get(rootFolder, file).toString();
            if (!zipEntries.containsKey(zipFile)) {
                throw new ZipFileValidationException(
                        String.format("Missing file: File %s not found in %s.", zipFile, zipFileName));
            }
        }
    }

    private void validateTableAccess(PackageFile packageFile, String rootFolder, Map<String, ZipEntry> zipEntries) throws ZipFileValidationException {
        String[] tableAccess = packageFile.getTableAccess();
        if (tableAccess == null || tableAccess.length == 0) {
            throw new ZipFileValidationException(
                    String.format("Table access files missing: %s.", zipFileName));
        }

        for (String file : tableAccess) {
            String zipFile = Paths.get(rootFolder, file).toString();
            if (!zipEntries.containsKey(zipFile)) {
                throw new ZipFileValidationException(
                        String.format("Missing file: File %s not found in %s.", zipFile, zipFileName));
            }
        }
    }

    private PackageFile getPackageFile(ZipEntry packageJsonZipEntry, ZipFile zipFile) throws ZipFileValidationException {
        final ObjectMapper objMapper = new ObjectMapper();
        try (InputStream is = zipFile.getInputStream(packageJsonZipEntry)) {
            return objMapper.readValue(is, PackageFile.class);
        } catch (IOException exception) {
            String errMsg = "Unable to map package.json in the zip file to PackageFile object.";
            LOGGER.error(errMsg, exception);
            throw new ZipFileValidationException(errMsg);
        }
    }

    private Map<String, ZipEntry> getZipEntries(ZipFile zipFile) {
        Map<String, ZipEntry> zipEntries = new HashMap<>();

        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getSize() > 0) {
                String entryName = entry.getName();
                if (entryName.endsWith("package.json")) {
                    zipEntries.put("package.json", entry);
                } else {
                    zipEntries.put(entry.getName(), entry);
                }
            }
        }

        return zipEntries;
    }

}
