/*
 * Copyright (c) 2026 Timotej Adamec
 * SPDX-License-Identifier: MIT
 *
 * This file is part of the thesis:
 * "Multiplatform snagging system with code sharing maximisation"
 *
 * Czech Technical University in Prague
 * Faculty of Information Technology
 * Department of Software Engineering
 *
 * Counterfactual artifact — experiment/commonize-photo.
 * Fuses OnlineDataResult + OfflineFirstDataResult variants into a single
 * commonMain shape so the photo-upload use case can be commonized. This
 * is the canonical DVT-violation widening — never introduced in the
 * main branch.
 */

package cz.adamec.timotej.snag.core.network.fe

sealed interface PhotoUploadResult<out T> {
    data class Success<T>(
        val data: T,
    ) : PhotoUploadResult<T>

    data class ProgrammerError(
        val throwable: Throwable,
    ) : PhotoUploadResult<Nothing>

    data object NetworkUnavailable : PhotoUploadResult<Nothing>

    data class UserMessageError(
        val throwable: Throwable,
        val message: String,
    ) : PhotoUploadResult<Nothing>
}
