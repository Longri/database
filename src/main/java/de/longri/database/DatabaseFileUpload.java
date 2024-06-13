/*
 * Copyright (C) 2024 Longri
 *
 * This file is part of fxutils.
 *
 * fxutils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * fxutils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with fxutils. If not, see <https://www.gnu.org/licenses/>.
 */
package de.longri.database;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Base64;


/**
 * put following php file to Server as upload.php
 * <p>
 * the upload/ folder musst have the rights "www-data:www-data" !
 * <p>
 * <p>
 * <?php
 * $target_dir = "upload/";
 * $delete_file = isset($_GET["delete_file"]) ? $_GET["delete_file"] : 0;
 * if ($delete_file != 0) {
 * echo "Delete File= $delete_file \n";
 * $delete_file = $target_dir . $delete_file;
 * if (file_exists($delete_file)) {
 * $status = unlink($delete_file) ? 'The file ' . $delete_file . ' has been deleted' : 'Error deleting ' . $delete_file;
 * echo $status;
 * } else {
 * echo "The file " . $delete_file . " doesnot exist";
 * }
 * } else {
 * $target_file = $target_dir . basename($_FILES["fileToUpload"]["name"]);
 * $uploadOk = 1;
 * $imageFileType = pathinfo($target_file, PATHINFO_EXTENSION);
 * // Check if image file is a actual image or fake image
 * if (isset($_POST["submit"])) {
 * $check = getimagesize($_FILES["fileToUpload"]["tmp_name"]);
 * if ($check !== false) {
 * echo "File is an image - " . $check["mime"] . ".";
 * $uploadOk = 1;
 * } else {
 * echo "File is not an image.";
 * $uploadOk = 0;
 * }
 * }
 * // Check if file already exists
 * if (file_exists($target_file)) {
 * echo "Sorry, file already exists.";
 * $uploadOk = 0;
 * }
 * // Check file size
 * if ($_FILES["fileToUpload"]["size"] > 500000) {
 * echo "Sorry, your file is too large.";
 * $uploadOk = 0;
 * }
 * // Allow certain file formats
 * if ($imageFileType != "jpg" && $imageFileType != "png" && $imageFileType != "jpeg"
 * && $imageFileType != "gif") {
 * echo "Sorry, only JPG, JPEG, PNG & GIF files are allowed.";
 * $uploadOk = 0;
 * }
 * // Check if $uploadOk is set to 0 by an error
 * if ($uploadOk == 0) {
 * echo "Sorry, your file was not uploaded.";
 * // if everything is ok, try to upload file
 * } else {
 * if (move_uploaded_file($_FILES["fileToUpload"]["tmp_name"], $target_file)) {
 * echo "The file " . basename($_FILES["fileToUpload"]["name"]) . " has been uploaded.";
 * } else {
 * echo "Sorry, there was an error uploading your file.";
 * }
 * }
 * }
 * ?>
 */
public class DatabaseFileUpload {

    private final String SERVER_IP;
    private final String UPLOAD_URL;
    private final String USER;
    private final String PASSWORD;

    public DatabaseFileUpload(String serverIp) {
        this(serverIp, null, null);
    }


    public DatabaseFileUpload(String serverIp, String user, String passwd) {
        SERVER_IP = serverIp;
        UPLOAD_URL = SERVER_IP + "/upload.php";
        USER = user;
        PASSWORD = passwd;
    }

    public int uploadImageToServer(File file) throws IOException {

        // the URL where the file will be posted
        String postReceiverUrl = "http://" + UPLOAD_URL;

        // new HttpClient
        HttpClient httpClient = HttpClientBuilder.create().build();

        // post header
        HttpPost httpPost = new HttpPost(postReceiverUrl);

        FileBody fileBody = new FileBody(file);

        //Set up HTTP post
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        reqEntity.addPart("fileToUpload", fileBody);

        reqEntity.addPart("delete_file", StringBody.create("qwe", "text/html", Charset.forName("utf-8")));


        httpPost.setEntity(reqEntity);

        String passwdstring = USER + ":" + PASSWORD;
        String encoding = Base64.getEncoder().encodeToString(passwdstring.getBytes());
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);

        // execute HTTP post request
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity resEntity = response.getEntity();

        if (resEntity != null) {

            String responseStr = EntityUtils.toString(resEntity).trim();


            // you can add an if statement here and do other actions based on the response
            System.out.println(responseStr);
            System.out.println(response.getStatusLine());
        }
        return 0;

    }

    public int deleteFromServer(String filename) throws URISyntaxException, IOException {

        // the URL where the file will be posted
        String postReceiverUrl = "http://" + UPLOAD_URL;

        URIBuilder builder = new URIBuilder(postReceiverUrl);
        builder.setParameter("delete_file", filename);

        HttpPost httpPost = new HttpPost(builder.build());
        String passwdstring = USER + ":" + PASSWORD;
        String encoding = Base64.getEncoder().encodeToString(passwdstring.getBytes());
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoding);

        // new HttpClient
        HttpClient httpClient = new DefaultHttpClient();


        // execute HTTP post request
        HttpResponse response = httpClient.execute(httpPost);
        HttpEntity resEntity = response.getEntity();

        if (resEntity != null) {

            String responseStr = EntityUtils.toString(resEntity).trim();


            // you can add an if statement here and do other actions based on the response
            System.out.println(responseStr);
            System.out.println(response.getStatusLine());
        }
        return 0;

    }

    public File getFile(String FILE_NAME) throws IOException {
        try {
            String[] arr = FILE_NAME.split("\\.(?=[^.]+$)");
            Path temp = Files.createTempFile(arr[0], "." + arr[1]);
            String FILE_URL = "http://" + SERVER_IP + "/upload/" + FILE_NAME;

            URL url = new URL(FILE_URL);
            String passwdstring = USER + ":" + PASSWORD;
            String encoding = Base64.getEncoder().encodeToString(passwdstring.getBytes());

            URLConnection uc = url.openConnection();
            uc.setRequestProperty("Authorization", "Basic " + encoding);

            InputStream in = (InputStream) uc.getInputStream();

            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            File tmpFile = temp.toFile();
            tmpFile.deleteOnExit();
            return tmpFile;
        } catch (FileNotFoundException e) {
            return null;
        }
    }

}
