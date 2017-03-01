package com.siams.ml.images.api.client;

import java.util.List;

/**
 * <p>
 * Created by alexei.vylegzhanin@gmail.com on 3/1/2017.
 */
public class RemoteImagesJavaTest {
    public static void main(String[] args) {
        final RemoteImages images = RemoteImages.Companion.connect(null, null, null);
        int imagesCountDown = 100;
        final List<FolderRef> folders = images.getFolders(images.getUser());
        for (FolderRef folder : folders) {
            System.out.println(folder);
            for (ProjectRef project : images.getProjects(folder)) {
                System.out.println("\t " + folder);
                if (project.isImage() && imagesCountDown-- > 0) {
                    final ImageRef image = images.openImage(project);
                    System.out.println("\t\t " + image);
                }
            }
        }
    }
}
