package com.example.domain.util

import com.example.domain.model.Load
import com.example.domain.model.Terminal

/**
 * Группирует список заявок по терминалу.
 * Заявки без указанного терминала игнорируются.
 */
fun List<Load>.groupByTerminal(): Map<Terminal, List<Load>> =
    filter { it.terminal != null }.
    groupBy { it.terminal!! }