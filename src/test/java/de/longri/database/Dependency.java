/*
 * Copyright (C) 2024 Longri
 *
 * This file is part of database.
 *
 * database is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * database is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with fxutils. If not, see <https://www.gnu.org/licenses/>.
 */
package de.longri.database;

import de.longri.filetransfer.Local_FileTransferHandle;
import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Dependency {

    public static String execCmd(String cmd) throws IOException {
        if (cmd == null || cmd.isEmpty()) return "";
        Process child = Runtime.getRuntime().exec(cmd);

        InputStream in = child.getInputStream();
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            sb.append((char) c);
        }
        in.close();
        return sb.toString();
    }


    @BeforeAll
    static void beforeAll() throws IOException {
        //Refresh dependency update plug in
        if (SystemUtils.IS_OS_WINDOWS)
            System.out.println(execCmd("./gradlew.bat dependencyUpdates"));
        else
            System.out.println(execCmd("./gradlew dependencyUpdates"));
    }


    String[][] IGNORE = new String[][]{
            new String[]{"junit-jupiter-api", "5.9.0-M1"}
            , new String[]{"junit-jupiter-engine", "5.9.0-M1"}
    };


    @Test
    void testDependency() throws IOException {
        // read report file
        Local_FileTransferHandle reportFile = new Local_FileTransferHandle("./build/dependencyUpdates/report.txt");
        assertTrue(reportFile.exists(), "Dependency Updates report file not created");

        String report = reportFile.readString("UTF-8");

        String gradle = getStringBetween(report, "Gradle release-candidate updates:", null, true);
        assertTrue(gradle.contains("UP-TO-DATE"), gradle);

        String failedVersion = getStringBetween(report, "Failed to compare versions", "Gradle release-candidate updates:", false);
        assertNull(failedVersion, failedVersion);

        String update = getStringBetween(report, "The following dependencies have later milestone versions:", "Gradle release-candidate updates:", false);
        if (update != null) {
            boolean removeLine = false;
            for (String[] ignore : IGNORE) {
                String[] updateLines = update.split("\n");
                for (String line : updateLines) {
                    if (removeLine) {
                        update = update.replace(line, "");
                        removeLine = false;
                        continue;
                    }
                    if (line.contains(ignore[0])) {
                        if (line.contains("-> " + ignore[1])) {
                            //ignore this and next line
                            removeLine = true;
                            update = update.replace(line, "");
                        }
                    }
                }
            }

            update = update.replaceAll("\n", "");
            update = update.replace("The following dependencies have later milestone versions:", "");
            if (update.isEmpty())
                update = null;
            else
                update = "The following dependencies have later milestone versions:\n" + update;
        }
        assertNull(update, update);
    }


    String getStringBetween(String from, String start, String end, boolean removeStartString) {
        int s = from.indexOf(start) + (removeStartString ? start.length() : 0);
        if (s < 0) return null;
        int e = end == null ? from.length() : from.indexOf(end, s);
        return from.substring(s, e);
    }

}