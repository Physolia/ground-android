/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gnd.repository;

import android.content.Context;
import android.net.Uri;
import com.google.android.gnd.persistence.remote.RemoteStorageManager;
import com.google.android.gnd.rx.annotations.Cold;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.reactivex.Single;
import java.io.File;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Provides access to user-provided media stored locally and remotely. This currently includes only
 * photos.
 */
public class UserMediaRepository {
  @Inject RemoteStorageManager remoteStorageManager;

  @Inject @ApplicationContext Context context;

  @Inject
  public UserMediaRepository() {}

  /**
   * Fetch url for the image from Firestore Storage. If the remote image is not available then
   * search for the file locally and return its uri.
   *
   * @param path Final destination path of the uploaded photo relative to Firestore
   */
  @Cold
  public Single<Uri> getDownloadUrl(String path) {
    return path.isEmpty()
        ? Single.just(Uri.EMPTY)
        : remoteStorageManager
            .getDownloadUrl(path)
            .onErrorReturn(__ -> getFileUriFromRemotePath(path));
  }

  private Uri getFileUriFromRemotePath(String destinationPath) {
    File file = getLocalFileFromRemotePath(destinationPath);
    if (file.exists()) {
      return Uri.fromFile(file);
    } else {
      Timber.d("File doesn't exist locally: %s", file.getPath());
      return Uri.EMPTY;
    }
  }

  /**
   * Returns the path of the file saved in the sdcard used for uploading to the provided destination
   * path.
   */
  public File getLocalFileFromRemotePath(String destinationPath) {
    String[] splits = destinationPath.split("/");
    String filename = splits[splits.length - 1];
    File file = new File(context.getFilesDir(), filename);
    if (!file.exists()) {
      Timber.e("File not found: %s", file.getPath());
    }
    return file;
  }
}
