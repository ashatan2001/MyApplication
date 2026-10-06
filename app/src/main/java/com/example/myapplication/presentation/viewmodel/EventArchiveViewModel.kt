package com.example.presentation.archive

import com.example.domain.model.Archive
import com.example.domain.model.Event
import com.example.domain.usecase.GetEventArchiveUseCase
import com.example.domain.util.CustomResult
import com.example.myapplication.presentation.viewmodel.BaseArchiveViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EventArchiveViewModel @Inject constructor(
    private val getEventArchiveUseCase: GetEventArchiveUseCase
) : BaseArchiveViewModel<Event>() {

    override suspend fun fetchArchive(
        startDate: String,
        endDate: String,
        pageNum: Int,
        records: Int
    ): CustomResult<Archive<Event>> {
        return getEventArchiveUseCase(
            startDate = startDate,
            endDate = endDate,
            pageNum = pageNum,
            records = records
        )
    }
}