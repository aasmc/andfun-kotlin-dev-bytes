/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.example.android.devbyteviewer.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.example.android.devbyteviewer.database.VideosDatabase
import com.example.android.devbyteviewer.database.asDomainModel
import com.example.android.devbyteviewer.domain.Video
import com.example.android.devbyteviewer.network.Network
import com.example.android.devbyteviewer.network.asDatabaseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository to fetch videos from the network and store them in the DB
 */
class VideosRepository(private val database: VideosDatabase) {

    /**
     * Property that stores List of videos retrieved from the DB and converted to Domain objects by way of
     * using Transformations.map and extension function
     */
    val videos: LiveData<List<Video>> = Transformations.map(database.videoDao.getVideos()) {
        it.asDomainModel()
    }

    /**
     * Suspend function that retrieves videos from the network on a background thread and
     * inserts them in the database
     * We use Dispatchers.IO to enable correct reading and writing info the the DB
     * * - is a spread operator that allows to pass arrays to methods that expect vararg parameters
     * We retrieve NetworkVideo DTOs from the Network and convert them to DatabaseVideo objects
     * before inserting them in the DB
     */
    suspend fun refreshVideos() {
        withContext(Dispatchers.IO) {
            val playlist = Network.devbytes.getPlaylist()
            database.videoDao.insertAll(*playlist.asDatabaseModel())
        }
    }
}