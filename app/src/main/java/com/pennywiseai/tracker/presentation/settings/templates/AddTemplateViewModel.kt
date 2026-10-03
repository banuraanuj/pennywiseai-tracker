package com.pennywiseai.tracker.presentation.settings.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pennywiseai.parser.core.DynamicTemplateParser
import com.pennywiseai.parser.core.ParsedTransaction
import com.pennywiseai.tracker.data.database.dao.CustomTemplateDao
import com.pennywiseai.tracker.data.database.entity.CustomTemplateEntity
import com.pennywiseai.tracker.data.database.entity.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTemplateViewModel @Inject constructor(
    private val dao: CustomTemplateDao
) : ViewModel() {

    private val _senderPattern = MutableStateFlow("")
    val senderPattern: StateFlow<String> = _senderPattern.asStateFlow()

    private val _messageTemplate = MutableStateFlow("")
    val messageTemplate: StateFlow<String> = _messageTemplate.asStateFlow()

    private val _transactionType = MutableStateFlow<TransactionType?>(null)
    val transactionType: StateFlow<TransactionType?> = _transactionType.asStateFlow()

    private val _testSms = MutableStateFlow("")
    val testSms: StateFlow<String> = _testSms.asStateFlow()

    private val _testSender = MutableStateFlow("")
    val testSender: StateFlow<String> = _testSender.asStateFlow()

    private val _parsedPreview = MutableStateFlow<ParsedTransaction?>(null)
    val parsedPreview: StateFlow<ParsedTransaction?> = _parsedPreview.asStateFlow()

    private var editingId: Long? = null

    fun loadTemplate(id: Long?) {
        if (id == null) return
        editingId = id
        viewModelScope.launch {
            dao.getTemplateById(id)?.let { t ->
                _senderPattern.value = t.senderPattern
                _messageTemplate.value = t.messageTemplate
                _transactionType.value = t.transactionType
            }
        }
    }

    fun updateSenderPattern(v: String) { _senderPattern.value = v; evaluatePreview() }
    fun updateMessageTemplate(v: String) { _messageTemplate.value = v; evaluatePreview() }
    fun updateTransactionType(v: TransactionType?) { _transactionType.value = v; evaluatePreview() }
    fun updateTestSms(v: String) { _testSms.value = v; evaluatePreview() }
    fun updateTestSender(v: String) { _testSender.value = v; evaluatePreview() }

    private fun evaluatePreview() {
        if (_messageTemplate.value.isBlank() || _testSms.value.isBlank()) {
            _parsedPreview.value = null
            return
        }
        val parser = DynamicTemplateParser()
        _parsedPreview.value = parser.parse(
            smsBody = _testSms.value,
            sender = _testSender.value.ifBlank { "TEST" },
            timestamp = System.currentTimeMillis(),
            template = _messageTemplate.value,
            forcedType = _transactionType.value?.let { com.pennywiseai.parser.core.TransactionType.valueOf(it.name) }
        )
    }

    fun saveTemplate(onDone: () -> Unit) {
        viewModelScope.launch {
            val entity = CustomTemplateEntity(
                id = editingId ?: 0L,
                senderPattern = _senderPattern.value.trim(),
                messageTemplate = _messageTemplate.value.trim(),
                transactionType = _transactionType.value
            )
            if (editingId != null) {
                dao.updateTemplate(entity)
            } else {
                dao.insertTemplate(entity)
            }
            onDone()
        }
    }

    fun deleteTemplate(onDone: () -> Unit) {
        viewModelScope.launch {
            editingId?.let { id ->
                dao.getTemplateById(id)?.let {
                    dao.deleteTemplate(it)
                }
            }
            onDone()
        }
    }
}