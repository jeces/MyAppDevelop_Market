package com.example.applicationjeces.product

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

// 테이블로 사용될 Notification 데이터 클래스를 정의합니다.
// 테이블 이름은 "notification_table"로 설정되었습니다.
@Entity(tableName = "notification_table")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Int,  // 자동으로 증가하는 기본 키
    val title: String,
    val message: String,
    val timestamp: String
)

// 데이터 액세스 객체 (DAO)를 정의합니다.
// 이 인터페이스는 데이터베이스에 액세스하기 위한 메서드들을 포함하고 있습니다.
@Dao
interface NotificationDao {
    // 알림을 데이터베이스에 추가하는 메서드입니다.
    // 동일한 id를 가진 알림이 이미 존재하는 경우, REPLACE 전략을 사용하여 기존 항목을 대체합니다.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNotification(notification: Notification)

    // 모든 알림을 가져오는 메서드입니다.
    // 결과는 LiveData로 반환되므로 UI가 알림 데이터의 변경 사항을 자동으로 업데이트 할 수 있습니다.
    @Query("SELECT * FROM notification_table ORDER BY id DESC")
    fun getAllNotifications(): LiveData<List<Notification>>
}

// 데이터베이스를 정의하는 추상 클래스입니다.
// 현재 버전은 1로 설정되어 있습니다.
@Database(entities = [Notification::class], version = 1)
abstract class NotificationDatabase: RoomDatabase() {
    // NotificationDao에 대한 추상 메서드입니다.
    // 이 메서드를 통해 데이터베이스 액세스 객체 (DAO)에 액세스 할 수 있습니다.
    abstract fun notificationDao(): NotificationDao

    companion object {
        // 싱글톤 패턴을 사용하여 앱 전체에서 하나의 데이터베이스 인스턴스만 생성하고 사용하도록 합니다.
        @Volatile
        private var INSTANCE: NotificationDatabase? = null

        // 싱글톤 인스턴스를 반환하거나, 없을 경우 새로 생성하여 반환하는 메서드입니다.
        fun getDatabase(context: Context): NotificationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotificationDatabase::class.java,
                    "notification_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
