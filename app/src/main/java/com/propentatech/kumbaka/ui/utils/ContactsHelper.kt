package com.propentatech.kumbaka.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract

object ContactsHelper {
    
    @SuppressLint("Range")
    fun getContactsList(context: Context): List<Pair<String, String>> {
        val contacts = mutableListOf<Pair<String, String>>()
        val uri = ContactsContract.Contacts.CONTENT_URI
        val projection = arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME)
        val sortOrder = "${ContactsContract.Contacts.DISPLAY_NAME} ASC"

        val cursor = context.contentResolver.query(uri, projection, null, null, sortOrder)

        cursor?.use {
            if (it.count > 0) {
                while (it.moveToNext()) {
                    val id = it.getString(it.getColumnIndex(ContactsContract.Contacts._ID))
                    val name = it.getString(it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    
                    if (name != null) {
                        contacts.add(Pair(id, name))
                    }
                }
            }
        }
        return contacts
    }
}
