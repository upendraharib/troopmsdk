package com.tvisha.troopmessenger.FileDeck.Model

import java.io.Serializable

data class FileModel(
    var total_size: Long = 0,
    var number_of_files: Int = 0,
    var number_of_folders: Int = 0,
    var ancestor: Int = 0,
    var descendant: Int = 0,
    var is_downloaded: Int = 0,
    var folder_id: Int = 0,
    var file_name: String? = null,
    var folder_name: String? = null,
    var folder_type: Int = 0,
    var created_at: String? = null,
    var file_path: String? = null,
    var tags: String? = null,
    var number_of_comments: Int = 0,
    var isFolder: Int = 0,
    var is_selected: Int = 0,

    var file_id: Int = 0,
    var fileExtension: String? = null,
    var file_size: Long = 0,
    var isImage: Int = 0,
    var isVideo: Int = 0,
    var isAudio: Int = 0,
    var type: Int = 0,
    var previewIcon: Int = 0,
    var preViewAvailable: Int = 0,
    var target: String? = null,
    var link: String? = null,

    var progress: String? = null,

    var tabId: Int=0


   /* var file_id: Int = 0,

    var fileExtention: String? = null,
    var fileSize: Double = 0.0,
    var filePath: String? = null,
    var createdAt: String? = null,
    var previewIcon: Int = 0,
    var downloadLink: String? = null,
    var preViewAvailable: Int =0,
    var isImage: Int = 0,
    var isVideo: Int = 0,
    var isAudio: Int = 0,
    var target: String? = null,
    var link: String? = null,
    var numberOfComments: Int = 0,
    var selected: Int = 0,
    var downloaded: Int = 0,
    var type: Int = 0,
    var progress: String? = null,
    var folderType: Int = 0,
    var numberOfFiles: Int = 0,
    var numberOfFolders: Int = 0,
    var workspaceID: String? = null,
    var folderAncestor: Int = 0,
    var folderDescendant: String? = null,
    var workspaceid: String? = null,
    var folderName: String? = null,
    var tabId: Int = 0,
    var isRoot :Int=0*/
) : Serializable
