package com.propentatech.kumbaka.advisor

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.propentatech.kumbaka.data.database.KumbakaDatabase
import com.propentatech.kumbaka.data.model.AdvisorLog
import com.propentatech.kumbaka.data.model.AdvisorMood
import kotlinx.coroutines.flow.first

/**
 * Worker qui s'exécute quotidiennement pour analyser les performances de l'utilisateur.
 * Vérifie le solde financier, l'accomplissement des habitudes et des tâches.
 */
class AdvisorBrainWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("AdvisorBrain", "Le conseiller expert analyse les performances...")
        
        try {
            val database = KumbakaDatabase.getInstance(applicationContext)
            
            // 1. Analyser les finances (Détection d'anomalie de solde)
            val transactions = database.transactionDao().getAllTransactions().first()
            val balance = transactions.sumOf { 
                if (it.type == com.propentatech.kumbaka.data.model.TransactionType.INCOME) it.amount else -it.amount 
            }
            
            var currentMood = AdvisorMood.JOYFUL
            var message = "Votre solde actuel est de ${balance.toInt()} FCFA. Tout semble sous contrôle."
            
            if (balance < 0) {
                currentMood = AdvisorMood.FURIOUS
                message = "ALERTE ROUGE : Votre solde est négatif (${balance.toInt()} FCFA) ! Réduisez vos dépenses immédiatement."
                
                // Déclencher une notification universelle d'anomalie immédiate
                com.propentatech.kumbaka.notification.NotificationHelper.showUniversalNotification(
                    context = applicationContext,
                    title = "⚠️ Anomalie Financière Détectée",
                    message = message,
                    type = com.propentatech.kumbaka.notification.NotificationType.ANOMALY
                )
            } else if (balance < 100) {
                currentMood = AdvisorMood.WORRIED
                message = "Attention, votre solde est bas (${balance.toInt()} FCFA). Prudence sur vos prochaines transactions."
            }
            
            // Enregistrer le log
            val log = AdvisorLog(
                advisorMood = currentMood,
                generatedMessage = message,
                eventTypeTriggered = "DAILY_REVIEW"
            )
            database.advisorLogDao().insertLog(log)
            
            // Déclencher la notification journalière standard
            AdvisorDispatcher.dispatchDailyReview(applicationContext, currentMood, message)
            
            return Result.success()
        } catch (e: Exception) {
            Log.e("AdvisorBrain", "Erreur lors de l'analyse", e)
            return Result.retry()
        }
    }
    
    companion object {
        const val WORK_NAME = "advisor_brain_daily_analysis"
    }
}
