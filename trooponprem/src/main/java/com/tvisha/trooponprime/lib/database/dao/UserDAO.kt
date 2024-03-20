package com.tvisha.trooponprime.lib.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteQuery
import com.tvisha.trooponprime.lib.clientModels.UserClientModel
import com.tvisha.trooponprime.lib.database.Messenger
import com.tvisha.trooponprime.lib.database.User
import com.tvisha.trooponprime.lib.database.model.*

@Dao
internal interface UserDAO {
    @Insert
    fun insert(user: User)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllUsers(users: List<User>)
    @Update
    fun update(user: User)
    @Query("DELETE FROM user")
    fun deleteUserTable()

    /*@Query("SELECT user_id FROM user WHERE user_id=:userId AND workspace_id =:workspaceId")
    fun checkWorkspaceUserExists(userId: String,workspaceId: String):Long*/
    @Query("SELECT user_id FROM user WHERE user_id=:userId")
    fun checkUserExists(userId: String):Long

    /*@Query("SELECT name FROM user WHERE user_id = :userId AND workspace_id = :workspaceId")
    fun fetchWorkspaceUseName(workspaceId:String,userId:String) :String*/
    @Query("SELECT name FROM user WHERE user_id = :userId")
    fun fetchUseName(userId:String) :String

    /*@Query("SELECT user_role FROM user WHERE user_id = :userId AND workspace_id = :workspaceId")
    fun fetchWorkspaceUserRole(userId: String,workspaceId: String):Int
    @Query("SELECT user_role FROM user WHERE user_id = :userId")
    fun fetchUserRole(userId: String):Int*/

    /*@Query("SELECT is_orange_member FROM user WHERE workspace_id = :workspaceId AND user_id=:userId")
    fun checkWorkspaceIsOrangeMember(workspaceId: String,userId: String) : Int
    @Query("SELECT is_orange_member FROM user WHERE user_id=:userId")
    fun checkIsOrangeMember(userId: String) : Int*/

    /*@Query("SELECT user_status FROM user WHERE user_id = :userId AND workspace_id = :workspaceId")
    fun checkWorkspaceIsUserActive(userId: String,workspaceId:String) :Int*/
    @Query("SELECT user_status FROM user WHERE user_id = :userId")
    fun checkIsUserActive(userId: String) :Int

    /*@Query("UPDATE user SET user_avatar= :userAvatar,updated_at =:updatedAt WHERE user_id=:userId AND workspace_id=:workspaceId")
    fun updateWorkspaceUserProfileAvatar(userId: String,userAvatar:String,workspaceId: String,updatedAt:String)*/
    @Query("UPDATE user SET profile_pic= :userAvatar,updated_at =:updatedAt WHERE user_id=:userId")
    fun updateUserProfileAvatar(userId: String,userAvatar:String,updatedAt:String)

    /*@Query("UPDATE user SET user_status= :status,updated_at =:updatedAt WHERE user_id=:userId AND workspace_id=:workspaceId")
    fun updateWorkspaceUserStatus(userId: String,status:Int,workspaceId: String,updatedAt:String)*/
    @Query("UPDATE user SET user_status= :status,updated_at =:updatedAt WHERE user_id=:userId")
    fun updateUserStatus(userId: String,status:Int,updatedAt:String)

    /*@Query("UPDATE user SET is_favourite = :favourite,updated_at = :updatedAt WHERE user_id = :entityId AND workspace_id =:workspaceId")
    fun updateWorkspaceUserFavouriteStatus(entityId:String,favourite:Int,workspaceId: String,updatedAt:String)
    @Query("UPDATE user SET is_favourite = :favourite,updated_at = :updatedAt WHERE user_id = :entityId")
    fun updateUserFavouriteStatus(entityId:String,favourite:Int,updatedAt:String)*/

    /*@Query("UPDATE user SET is_muted = :muteStatus,updated_at = :updatedAt WHERE user_id = :entityId AND workspace_id =:workspaceId")
    fun updateWorkspaceUserMuteStatus(entityId:String,muteStatus:Int,workspaceId: String,updatedAt: String)
    @Query("UPDATE user SET is_muted = :muteStatus,updated_at = :updatedAt WHERE user_id = :entityId")
    fun updateUserMuteStatus(entityId:String,muteStatus:Int,updatedAt: String)*/

    /*@Query("UPDATE user SET unread_count = :count WHERE user_id = :userId AND workspace_id=:workspaceId")
    fun updateWorkspaceUserUnreadCount(count:Int,userId: String,workspaceId: String)*/
    @Query("UPDATE user SET unread_messages_count = :count WHERE user_id = :userId")
    fun updateUserUnreadCount(count:Int,userId: String)

    /*@Query("UPDATE user SET respond_later_count = :count WHERE user_id = :userId AND workspace_id=:workspaceId")
    fun updateWorkspaceUserRespondLaterCount(count:Int,userId: String,workspaceId: String)
    @Query("UPDATE user SET respond_later_count = :count WHERE user_id = :userId")
    fun updateUserRespondLaterCount(count:Int,userId: String)*/

    /*@Query("UPDATE user SET read_receipt_count = :count WHERE user_id = :userId AND workspace_id=:workspaceId")
    fun updateWorkspaceUserReadReceiptCount(count:Int,userId: String,workspaceId: String)
    @Query("UPDATE user SET read_receipt_count = :count WHERE user_id = :userId")
    fun updateUserReadReceiptCount(count:Int,userId: String)*/

    /*@Query("SELECT * FROM user WHERE user_id = :userId AND workspace_id = :workspaceId")
    fun fetchWorkspaceUser(workspaceId: String,userId: String) :User*/
    @Query("SELECT * FROM user WHERE user_id = :userId ")
    fun fetchUser(userId: String) :User

    /*@Query("UPDATE user SET unread_count = (SELECT unread_count FROM user WHERE user_id = :userId AND workspace_id = :workspaceId )+1 WHERE user_id = :userId AND workspace_id = :workspaceId")
    fun increaseWorkspaceUnreadUserCount(userId: String,workspaceId: String)*/
    @Query("UPDATE user SET unread_messages_count = (SELECT unread_messages_count FROM user WHERE user_id = :userId)+1 WHERE user_id = :userId")
    fun increaseUnreadUserCount(userId: String)

    /*@Query("UPDATE user SET unread_count = (SELECT unread_count FROM user WHERE user_id = :userId AND workspace_id = :workspaceId )-1 WHERE user_id = :userId AND workspace_id = :workspaceId AND unread_count>0")
    fun decreaseWorkspaceUnreadUserCount(userId: String,workspaceId: String)*/
    @Query("UPDATE user SET unread_messages_count = (SELECT unread_messages_count FROM user WHERE user_id = :userId )-1 WHERE user_id = :userId  AND unread_messages_count>0")
    fun decreaseUnreadUserCount(userId: String)

    /*@Query("UPDATE user SET respond_later_count = (SELECT respond_later_count FROM user WHERE user_id = :userId AND workspace_id = :workspaceId )+1 WHERE user_id = :userId AND workspace_id = :workspaceId")
    fun increaseWorkspaceRespondLaterUserCount(userId: String,workspaceId: String)
    @Query("UPDATE user SET respond_later_count = (SELECT respond_later_count FROM user WHERE user_id = :userId )+1 WHERE user_id = :userId ")
    fun increaseRespondLaterUserCount(userId: String)*/

    /*@Query("UPDATE user SET respond_later_count = (SELECT respond_later_count FROM user WHERE user_id = :userId AND workspace_id = :workspaceId )-1 WHERE user_id = :userId AND workspace_id = :workspaceId AND respond_later_count>0")
    fun decreaseWorkspaceRespondLaterUserCount(userId: String,workspaceId: String)
    @Query("UPDATE user SET respond_later_count = (SELECT respond_later_count FROM user WHERE user_id = :userId  )-1 WHERE user_id = :userId  AND respond_later_count>0")
    fun decreaseRespondLaterUserCount(userId: String)*/

    /*@Query("UPDATE user SET read_receipt_count = (SELECT read_receipt_count FROM user WHERE user_id = :userId AND workspace_id = :workspaceId )+1 WHERE user_id = :userId AND workspace_id = :workspaceId")
    fun increaseWorkspaceReadReceiptUserCount(userId: String,workspaceId: String)
    @Query("UPDATE user SET read_receipt_count = (SELECT read_receipt_count FROM user WHERE user_id = :userId)+1 WHERE user_id = :userId")
    fun increaseReadReceiptUserCount(userId: String)*/

    /*@Query("UPDATE user SET read_receipt_count = (SELECT read_receipt_count FROM user WHERE user_id = :userId AND workspace_id = :workspaceId )-1 WHERE user_id = :userId AND workspace_id = :workspaceId AND read_receipt_count>0")
    fun decreaseWorkspaceReadReceiptUserCount(userId: String,workspaceId: String)
    @Query("UPDATE user SET read_receipt_count = (SELECT read_receipt_count FROM user WHERE user_id = :userId )-1 WHERE user_id = :userId  AND read_receipt_count>0")
    fun decreaseReadReceiptUserCount(userId: String)*/

    /*@Query("SELECT 0 as clickable, 1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id <> :workspaceUserId and user_status =1  and workspace_id = :workspaceId and is_orange_member IN (:orangeMember) GROUP BY entity_id HAVING ((orange IN (1) AND entity_id IN (:orange)) OR (orange IN (0) AND entity_id IN (:users) )) order by lower (name) ASC")
    fun fetchWorkspaceAllUsers(workspaceId: String,workspaceUserId:String,users:List<String>,orange:List<String>,orangeMember:List<String>): LiveData<List<ContactModel>>*/
    /*@Query("SELECT 0 as clickable, 1 as entity_type,cast(user_id as char) as entity_id,name,mobile,user_avatar,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id <> :loginUserId and user_status =1  GROUP BY entity_id HAVING ((entity_id IN (:orange)) OR (entity_id IN (:users) )) order by lower (name) ASC")
    fun fetchAllUsers(loginUserId:String,users:List<String>,orange:List<String>,orangeMember:List<String>): LiveData<List<ContactModel>>*/

    /*@Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,name,mobile,user_avatar,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id <> :workspaceUserId and user_status =1  and workspace_id = :workspaceId and is_orange_member IN (:orangeMember) GROUP BY entity_id HAVING ((orange IN (1)) OR (orange IN (0) AND entity_id IN (:users) )) order by lower (name) ASC")
    fun fetchWorkspaceAllUsersWithUsers(workspaceId: String,workspaceUserId:String,users:List<String>,orangeMember:List<String>):LiveData<List<ContactModel>>*/
    /*@Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,name,mobile,user_avatar,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id <> :loginUserId and user_status =1  GROUP BY entity_id HAVING (() OR (entity_id IN (:users) )) order by lower (name) ASC")
    fun fetchAllUsersWithUsers(loginUserId: String,users:List<String>,orangeMember:List<String>):LiveData<List<ContactModel>>*/

    /*@Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id <> :workspaceUserId and user_status =1  and workspace_id = :workspaceId and is_orange_member IN (:orangeMember) GROUP BY entity_id HAVING ((orange IN (1) AND entity_id IN (:orange))  OR (orange IN (0)  )) order by lower (name) ASC")
    fun fetchWorkspaceAllUsersWithOrange(workspaceId: String,workspaceUserId:String,orange:List<String>,orangeMember:List<String>):LiveData<List<ContactModel>>
    @Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id <> :loginUserId and user_status =1  and is_orange_member IN (:orangeMember) GROUP BY entity_id HAVING ((orange IN (1) AND entity_id IN (:orange))  OR (orange IN (0)  )) order by lower (name) ASC")
    fun fetchAllUsersWithOrange(loginUserId: String,orange:List<String>,orangeMember:List<String>):LiveData<List<ContactModel>>*/
    /*@Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,0 as user_role,name,'' as email,mobile,0 as is_favourite,0 as is_muted,user_avatar,0 as orange,'' as workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id <> :loginUserId and user_status =1  and orange IN (:orangeMember) GROUP BY entity_id HAVING ((orange IN (1) AND entity_id IN (:orange))  OR (orange IN (0)  )) order by lower (name) ASC")
    fun fetchAllUsersWithOrange(loginUserId: String,orange:List<String>,orangeMember:List<String>):LiveData<List<ContactModel>>*/

    /*@Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id <> :workspaceUserId and user_status =1  and workspace_id = :workspaceId and is_orange_member IN (:orangeMember) GROUP BY entity_id HAVING ((orange IN (1) ) OR (orange IN (0) )) order by lower (name) ASC")
    fun fetchWorkspaceAllContacts(workspaceId: String,workspaceUserId:String,orangeMember:List<String>):LiveData<List<ContactModel>>
    @Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id <> :loginUserId and user_status =1  and is_orange_member IN (:orangeMember) GROUP BY entity_id HAVING ((orange IN (1) ) OR (orange IN (0) )) order by lower (name) ASC")
    fun fetchAllContacts(loginUserId: String,orangeMember:List<String>):LiveData<List<ContactModel>>*/
    /*@Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,0 as user_role,name,'' as email,mobile,0 as is_favourite,0 as is_muted,user_avatar,0 as orange,'' as workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id <> :loginUserId and user_status =1  GROUP BY entity_id HAVING ((orange IN (1) ) OR (orange IN (0) )) order by lower (name) ASC")
    fun fetchAllContacts(loginUserId: String,orangeMember:List<String>):LiveData<List<ContactModel>>*/

    /*@Query("SELECT 0 as clickable,1 as entity_type,user_id as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id = :workspaceUserId and user_status =1  and workspace_id = :workspaceId")
    fun fetchWorkspaceContact(workspaceId: String,workspaceUserId:String):ContactModel
    @Query("SELECT 0 as clickable,1 as entity_type,user_id as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id = :loginUserId and user_status =1")
    fun fetchContact(loginUserId: String):ContactModel*/
    /*@Query("SELECT 0 as clickable,1 as entity_type,user_id as entity_id,0 as user_role,name,'' as email,mobile,0 as is_favourite,0 as is_muted,user_avatar,0 as orange,'' as workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_id = :loginUserId and user_status =1")
    fun fetchContact(loginUserId: String):ContactModel*/

    /*@Query("SELECT 0 as clickable,1 as entity_type,is_orange_member as orange,u.workspace_id,u.user_id as entity_id,u.is_favourite,u.is_muted,u.name,u.email,u.mobile,u.user_avatar,gm.user_role AS user_role,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked FROM user AS u INNER JOIN group_member AS gm ON gm.user_id = u.user_id AND gm.workspace_id = u.workspace_id WHERE gm.group_id =:entityId AND gm.user_status = 1 AND  u.user_status = 1 AND u.workspace_id = :workspaceId GROUP BY entity_id  ORDER BY u.name ASC")
    fun fetchWorkspaceReadReceiptWithOrangeUsers(workspaceId: String,entityId: String):LiveData<List<ContactModel>>
    @Query("SELECT 0 as clickable,1 as entity_type,is_orange_member as orange,u.workspace_id,u.user_id as entity_id,u.is_favourite,u.is_muted,u.name,u.email,u.mobile,u.user_avatar,gm.user_role AS user_role,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked FROM user AS u INNER JOIN group_member AS gm ON gm.user_id = u.user_id AND gm.workspace_id = u.workspace_id WHERE gm.group_id =:entityId AND gm.user_status = 1 AND  u.user_status = 1  GROUP BY entity_id  ORDER BY u.name ASC")
    fun fetchReadReceiptWithOrangeUsers(entityId: String):LiveData<List<ContactModel>>*/
    /*@Query("SELECT 0 as clickable,1 as entity_type,0 as orange,'' as workspace_id,u.user_id as entity_id,0 as is_favourite,0 as is_muted,u.name,'' as email,u.mobile,u.user_avatar,gm.user_role AS user_role,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked FROM user AS u INNER JOIN group_member AS gm ON gm.user_id = u.user_id  WHERE gm.group_id =:entityId AND gm.user_status = 1 AND  u.user_status = 1  GROUP BY entity_id  ORDER BY u.name ASC")
    fun fetchReadReceiptWithOrangeUsers(entityId: String):LiveData<List<ContactModel>>*/

    /*@Query("SELECT 0 as clickable,1 as entity_type,is_orange_member as orange,u.workspace_id,u.user_id as entity_id,u.is_favourite,u.is_muted,u.name,u.email,u.mobile,u.user_avatar,gm.user_role AS user_role,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked FROM user AS u INNER JOIN group_member AS gm ON gm.user_id = u.user_id AND gm.workspace_id = u.workspace_id WHERE gm.group_id =:entityId AND u.is_orange_member=0 AND gm.user_status = 1 AND  u.user_status = 1 AND u.workspace_id = :workspaceId GROUP BY entity_id  ORDER BY u.name ASC")
    fun fetchWorkspaceReadReceiptWithOutOrangeUsers(workspaceId: String,entityId: String):LiveData<List<ContactModel>>
    @Query("SELECT 0 as clickable,1 as entity_type,is_orange_member as orange,u.workspace_id,u.user_id as entity_id,u.is_favourite,u.is_muted,u.name,u.email,u.mobile,u.user_avatar,gm.user_role AS user_role,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked FROM user AS u INNER JOIN group_member AS gm ON gm.user_id = u.user_id  WHERE gm.group_id =:entityId AND u.is_orange_member=0 AND gm.user_status = 1 AND  u.user_status = 1  GROUP BY entity_id  ORDER BY u.name ASC")
    fun fetchReadReceiptWithOutOrangeUsers(entityId: String):LiveData<List<ContactModel>>*/
    /*@Query("SELECT 0 as clickable,1 as entity_type,0 as orange,'' as workspace_id,u.user_id as entity_id,0 as is_favourite,0 as is_muted,u.name,'' as email,u.mobile,u.user_avatar,gm.user_role AS user_role,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked FROM user AS u INNER JOIN group_member AS gm ON gm.user_id = u.user_id  WHERE gm.group_id =:entityId  AND gm.user_status = 1 AND  u.user_status = 1  GROUP BY entity_id  ORDER BY u.name ASC")
    fun fetchReadReceiptWithOutOrangeUsers(entityId: String):LiveData<List<ContactModel>>*/

    /*@Query("SELECT 0 as isSelected,0 as isSelected,2 as type,0 as user_count,0 as group_type,0 as entity_type,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,cast(user_id as char) as entity_id,is_orange_member as orange,user_status as status from user where (user_id != :workspaceUserId and user_status = 1 and is_favourite IN (1) and workspace_id = :workspaceId) group by entity_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) AND entity_id IN (:orange)) OR (orange IN (0) AND entity_id IN (:users)))))) order by name asc")
    fun fetchWorkspaceFavouriteUserList(workspaceId: String,workspaceUserId: String,orange: List<String>,users: List<String>):List<ForkOutModel>
    @Query("SELECT 0 as isSelected,0 as isSelected,2 as type,0 as user_count,0 as group_type,0 as entity_type,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,cast(user_id as char) as entity_id,is_orange_member as orange,user_status as status from user where (user_id != :loginUserId and user_status = 1 and is_favourite IN (1)) group by entity_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) AND entity_id IN (:orange)) OR (orange IN (0) AND entity_id IN (:users)))))) order by name asc")
    fun fetchFavouriteUserList(loginUserId: String,orange: List<String>,users: List<String>):List<ForkOutModel>*/
    /*@Query("SELECT 0 as isSelected,0 as isSelected,2 as type,0 as user_count,0 as group_type,0 as entity_type,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,0 as is_favourite,0 as is_muted,cast(user_id as char) as entity_id,0 as orange,user_status as status from user where (user_id != :loginUserId and user_status = 1 and is_favourite IN (1)) group by entity_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) AND entity_id IN (:orange)) OR (orange IN (0) AND entity_id IN (:users)))))) order by name asc")
    fun fetchFavouriteUserList(loginUserId: String,orange: List<String>,users: List<String>):List<ForkOutModel>*/

    /*@Query("SELECT 0 as isSelected,2 as type,0 as user_count,0 as group_type,0 as entity_type,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,cast(user_id as char) as entity_id,is_orange_member as orange,user_status as status from user where (user_id != :workspaceUserId and user_status = 1 and is_favourite IN (1) and workspace_id = :workspaceId) group by entity_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) AND entity_id IN (:orange)) OR (orange IN (0)))))) order by name asc")
    fun fetchWorkspaceFavouriteUserListOrange(workspaceId: String,workspaceUserId: String,orange: List<String>):List<ForkOutModel>
    @Query("SELECT 0 as isSelected,2 as type,0 as user_count,0 as group_type,0 as entity_type,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,cast(user_id as char) as entity_id,is_orange_member as orange,user_status as status from user where (user_id != :loginUserId and user_status = 1 and is_favourite IN (1)) group by entity_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) AND entity_id IN (:orange)) OR (orange IN (0)))))) order by name asc")
    fun fetchFavouriteUserListOrange(loginUserId: String,orange: List<String>):List<ForkOutModel>*/


    /*@Query("SELECT 0 as isSelected,2 as type,0 as user_count,0 as group_type,0 as entity_type,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,cast(user_id as char) as entity_id,is_orange_member as orange,user_status as status from user where (user_id != :workspaceUserId and user_status = 1 and is_favourite IN (1) and workspace_id = :workspaceId) group by entity_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) ) OR (orange IN (0) AND entity_id IN (:users)))))) order by name asc")
    fun fetchWorkspaceFavouriteUserListUser(workspaceId: String,workspaceUserId: String,users: List<String>):List<ForkOutModel>
    @Query("SELECT 0 as isSelected,2 as type,0 as user_count,0 as group_type,0 as entity_type,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,cast(user_id as char) as entity_id,is_orange_member as orange,user_status as status from user where (user_id != :loginUserId and user_status = 1 and is_favourite IN (1)) group by entity_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) ) OR (orange IN (0) AND entity_id IN (:users)))))) order by name asc")
    fun fetchFavouriteUserListUser(loginUserId: String,users: List<String>):List<ForkOutModel>*/

    /*@Query("SELECT 0 as isSelected,2 as type,0 as user_count,0 as group_type,0 as entity_type,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,cast(user_id as char) as entity_id,is_orange_member as orange,user_status as status from user where (user_id != :workspaceUserId and user_status = 1 and is_favourite IN (1) and workspace_id = :workspaceId) group by entity_id  order by name asc")
    fun fetchWorkspaceUserFavouriteList(workspaceId: String,workspaceUserId: String):List<ForkOutModel>
    @Query("SELECT 0 as isSelected,2 as type,0 as user_count,0 as group_type,0 as entity_type,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,cast(user_id as char) as entity_id,is_orange_member as orange,user_status as status from user where (user_id != :loginUserId and user_status = 1 and is_favourite IN (1)) group by entity_id  order by name asc")
    fun fetchUserFavouriteList(loginUserId: String):List<ForkOutModel>*/


    /*@Query("SELECT 0 as isSelected,3 as type,0 as user_count,0 as group_type,0 as entity_type,cast(user_id as char) as entity_id,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,is_orange_member as orange,user_status as status from user where user_status = 1 and workspace_id = :workspaceId GROUP BY user_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) AND entity_id IN (:orange)) OR (orange IN (0) AND entity_id IN (:users)))))) order by name asc")
    fun fetchWorkspaceForkOutUserList(workspaceId: String,orange: List<String>,users: List<String>):List<ForkOutModel>
    @Query("SELECT 0 as isSelected,3 as type,0 as user_count,0 as group_type,0 as entity_type,cast(user_id as char) as entity_id,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,is_orange_member as orange,user_status as status from user where user_status = 1  GROUP BY user_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) AND entity_id IN (:orange)) OR (orange IN (0) AND entity_id IN (:users)))))) order by name asc")
    fun fetchForkOutUserList(orange: List<String>,users: List<String>):List<ForkOutModel>*/

    /*@Query("SELECT 0 as isSelected,3 as type,0 as user_count,0 as group_type,0 as entity_type,cast(user_id as char) as entity_id,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,is_orange_member as orange,user_status as status from user where user_status = 1 and workspace_id = :workspaceId GROUP BY user_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) AND entity_id IN (:orange)) OR (orange IN (0)))))) order by name asc")
    fun fetchWorkspaceForkOutUserListOrange(workspaceId: String,orange: List<String>):List<ForkOutModel>
    @Query("SELECT 0 as isSelected,3 as type,0 as user_count,0 as group_type,0 as entity_type,cast(user_id as char) as entity_id,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,is_orange_member as orange,user_status as status from user where user_status = 1  GROUP BY user_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) AND entity_id IN (:orange)) OR (orange IN (0)))))) order by name asc")
    fun fetchForkOutUserListOrange(orange: List<String>):List<ForkOutModel>*/


    /*@Query("SELECT 0 as isSelected,3 as type,0 as user_count,0 as group_type,0 as entity_type,cast(user_id as char) as entity_id,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,is_orange_member as orange,user_status as status from user where user_status = 1 and workspace_id = :workspaceId GROUP BY user_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) ) OR (orange IN (0) AND entity_id IN (:users)))))) order by name asc")
    fun fetchWorkspaceForkOutUserListUser(workspaceId: String,users: List<String>):List<ForkOutModel>
    @Query("SELECT 0 as isSelected,3 as type,0 as user_count,0 as group_type,0 as entity_type,cast(user_id as char) as entity_id,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,is_orange_member as orange,user_status as status from user where user_status = 1  GROUP BY user_id HAVING (entity_type=1 OR (((entity_type=0 AND status = 1) AND ((orange IN (1) ) OR (orange IN (0) AND entity_id IN (:users)))))) order by name asc")
    fun fetchForkOutUserListUser(users: List<String>):List<ForkOutModel>*/


    /*@Query("SELECT 0 as isSelected,3 as type,0 as user_count,0 as group_type,0 as entity_type,cast(user_id as char) as entity_id,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,is_orange_member as orange,user_status as status from user where user_status = 1 and workspace_id = :workspaceId GROUP BY user_id  order by name asc")
    fun fetchWorkspaceUserForkOut(workspaceId: String):List<ForkOutModel>
    @Query("SELECT 0 as isSelected,3 as type,0 as user_count,0 as group_type,0 as entity_type,cast(user_id as char) as entity_id,name as entity_name,name as search_name,created_at,user_avatar as entity_avatar,user.is_favourite,user.is_muted,is_orange_member as orange,user_status as status from user where user_status = 1  GROUP BY user_id  order by name asc")
    fun fetchUserForkOut():List<ForkOutModel>*/


    @Query("SELECT updated_at from user ORDER BY updated_at DESC limit 1")
    fun fetchLastSyncTime():String

    /*@Query("SELECT created_at from user WHERE workspace_id= :workspaceId ORDER BY created_at DESC limit 1")
    fun fetchWorkspaceLastSync(workspaceId: String):String*/

    /*@Query("SELECT ID,cast(user_id as char) as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,-1 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,is_muted as muted,is_favourite as favourite,0 as entity_type,0 as message_status,is_orange_member as orange,user_status as userstatus,0 as group_type,null as message,(:createdAt) as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_id NOT IN (:messengerUsers) AND user_status = 1 AND workspace_id =:workspaceId GROUP BY receiver HAVING (entity_type=1 OR (((entity_type=0 AND userstatus = 1) AND ((orange IN (1) AND receiver IN (:orange)) OR (orange IN (0) AND receiver IN (:users)))))) ORDER BY lower(entity_name) ASC")
    fun fetchWorkspaceRecentList(workspaceId: String,users:List<String>,orange: List<String>,messengerUsers:List<String>,createdAt:String) : LiveData<List<RecentList>>
    @Query("SELECT ID,cast(user_id as char) as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,-1 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,is_muted as muted,is_favourite as favourite,0 as entity_type,0 as message_status,is_orange_member as orange,user_status as userstatus,0 as group_type,null as message,(:createdAt) as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_id NOT IN (:messengerUsers) AND user_status = 1  GROUP BY receiver HAVING (entity_type=1 OR (((entity_type=0 AND userstatus = 1) AND ((orange IN (1) AND receiver IN (:orange)) OR (orange IN (0) AND receiver IN (:users)))))) ORDER BY lower(entity_name) ASC")
    fun fetchRecentList(users:List<String>,orange: List<String>,messengerUsers:List<String>,createdAt:String) : LiveData<List<RecentList>>*/
    /*@Query("SELECT ID,cast(user_id as char) as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,-1 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,0 as muted,0 as favourite,0 as entity_type,0 as message_status,0 as orange,user_status as userstatus,0 as group_type,null as message,(:createdAt) as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,'' as workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_id NOT IN (:messengerUsers) AND user_status = 1  GROUP BY receiver HAVING (entity_type=1 OR (((entity_type=0 AND userstatus = 1) AND ((orange IN (1) AND receiver IN (:orange)) OR (orange IN (0) AND receiver IN (:users)))))) ORDER BY lower(entity_name) ASC")
    fun fetchRecentList(users:List<String>,orange: List<String>,messengerUsers:List<String>,createdAt:String) : LiveData<List<RecentList>>*/

    /*@Query("SELECT ID,cast(user_id as char) as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,-1 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,is_muted as muted,is_favourite as favourite,0 as entity_type,0 as message_status,is_orange_member as orange,user_status as userstatus,0 as group_type,null as message,(:createdAt) as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_id NOT IN (:messengerUsers) AND user_status = 1 AND workspace_id =:workspaceId GROUP BY receiver HAVING (entity_type=1 OR (((entity_type=0 AND userstatus = 1) AND ((orange IN (1) AND receiver IN (:orange)) OR (orange IN (0)))))) ORDER BY lower(entity_name) ASC ")
    fun fetchWorkspaceRecentListWithOrange(workspaceId: String,orange: List<String>,messengerUsers:List<String>,createdAt: String) : LiveData<List<RecentList>>
    @Query("SELECT ID,cast(user_id as char) as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,-1 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,is_muted as muted,is_favourite as favourite,0 as entity_type,0 as message_status,is_orange_member as orange,user_status as userstatus,0 as group_type,null as message,(:createdAt) as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_id NOT IN (:messengerUsers) AND user_status = 1  GROUP BY receiver HAVING (entity_type=1 OR (((entity_type=0 AND userstatus = 1) AND ((orange IN (1) AND receiver IN (:orange)) OR (orange IN (0)))))) ORDER BY lower(entity_name) ASC ")
    fun fetchRecentListWithOrange(orange: List<String>,messengerUsers:List<String>,createdAt: String) : LiveData<List<RecentList>>*/
    /*@Query("SELECT ID,cast(user_id as char) as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,-1 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,0 as muted,0 as favourite,0 as entity_type,0 as message_status,0 as orange,user_status as userstatus,0 as group_type,null as message,(:createdAt) as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,'' as workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_id NOT IN (:messengerUsers) AND user_status = 1  GROUP BY receiver HAVING (entity_type=1 OR (((entity_type=0 AND userstatus = 1) AND ((orange IN (1) AND receiver IN (:orange)) OR (orange IN (0)))))) ORDER BY lower(entity_name) ASC ")
    fun fetchRecentListWithOrange(orange: List<String>,messengerUsers:List<String>,createdAt: String) : LiveData<List<RecentList>>*/

    /*@Query("SELECT ID,cast(user_id as char) as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,-1 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,is_muted as muted,is_favourite as favourite,0 as entity_type,0 as message_status,is_orange_member as orange,user_status as userstatus,0 as group_type,null as message,(:createdAt) as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_id NOT IN (:messengerUsers) AND user_status = 1 AND workspace_id =:workspaceId GROUP BY receiver HAVING (entity_type=1 OR (((entity_type=0 AND userstatus = 1) AND ((orange IN (1)) OR (orange IN (0) AND receiver IN (:users)))))) ORDER BY lower(entity_name) ASC ")
    fun fetchWorkspaceRecentListWithUser(workspaceId: String,users: List<String>,messengerUsers:List<String>,createdAt: String) : LiveData<List<RecentList>>
    @Query("SELECT ID,cast(user_id as char) as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,-1 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,is_muted as muted,is_favourite as favourite,0 as entity_type,0 as message_status,is_orange_member as orange,user_status as userstatus,0 as group_type,null as message,(:createdAt) as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_id NOT IN (:messengerUsers) AND user_status = 1  GROUP BY receiver HAVING (entity_type=1 OR (((entity_type=0 AND userstatus = 1) AND ((orange IN (1)) OR (orange IN (0) AND receiver IN (:users)))))) ORDER BY lower(entity_name) ASC ")
    fun fetchRecentListWithUser(users: List<String>,messengerUsers:List<String>,createdAt: String) : LiveData<List<RecentList>>*/
    /*@Query("SELECT ID,cast(user_id as char) as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,-1 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,0 as muted,0 as favourite,0 as entity_type,0 as message_status,0 as orange,user_status as userstatus,0 as group_type,null as message,(:createdAt) as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,'' as workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_id NOT IN (:messengerUsers) AND user_status = 1  GROUP BY receiver HAVING (entity_type=1 OR (((entity_type=0 AND userstatus = 1) AND ((orange IN (1)) OR (orange IN (0) AND receiver IN (:users)))))) ORDER BY lower(entity_name) ASC ")
    fun fetchRecentListWithUser(users: List<String>,messengerUsers:List<String>,createdAt: String) : LiveData<List<RecentList>>*/

    /*@Query("SELECT ID,cast(user_id as char) as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,-1 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,is_muted as muted,is_favourite as favourite,0 as entity_type,0 as message_status,is_orange_member as orange,user_status as userstatus,0 as group_type,null as message,(:createdAt) as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_id NOT IN (:messengerUsers) AND user_status = 1 AND workspace_id =:workspaceId GROUP BY receiver  ORDER BY lower(entity_name) ASC ")
    fun fetchWorkspaceRecent(workspaceId: String,messengerUsers:List<String>,createdAt: String) : LiveData<List<RecentList>>
    @Query("SELECT ID,cast(user_id as char) as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,-1 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,is_muted as muted,is_favourite as favourite,0 as entity_type,0 as message_status,is_orange_member as orange,user_status as userstatus,0 as group_type,null as message,(:createdAt) as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_id NOT IN (:messengerUsers) AND user_status = 1 GROUP BY receiver  ORDER BY lower(entity_name) ASC ")
    fun fetchRecent(messengerUsers:List<String>,createdAt: String) : LiveData<List<RecentList>>*/
    /*@Query("SELECT ID,cast(user_id as char) as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,-1 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,0 as muted,0 as favourite,0 as entity_type,0 as message_status,0 as orange,user_status as userstatus,0 as group_type,null as message,(:createdAt) as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,'' as workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_id NOT IN (:messengerUsers) AND user_status = 1 GROUP BY receiver  ORDER BY lower(entity_name) ASC ")
    fun fetchRecent(messengerUsers:List<String>,createdAt: String) : LiveData<List<RecentList>>*/

    /*@Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted, 0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,name,email,user_avatar as userpic,user_status,is_orange_member as isOrangeMember,cast(user_id as char) as member_id,user_id FROM user WHERE user_id!=:workspaceUserId AND user_status=1 AND workspace_id = :workspaceId GROUP BY user_id HAVING (((user_status = 1) AND ((isOrangeMember IN (1) AND member_id IN (:orange)) OR (isOrangeMember IN (0) AND member_id IN (:users))))) ORDER BY LOWER(name) ASC " )
    fun fetchWorkspaceParticipantsWithUserOrange(workspaceId: String, workspaceUserId: String, users: List<String>, orange: List<String>):List<ParticipantUsers>
    @Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted, 0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,name,email,user_avatar as userpic,user_status,is_orange_member as isOrangeMember,cast(user_id as char) as member_id,user_id FROM user WHERE user_id!=:loginUserId AND user_status=1  GROUP BY user_id HAVING (((user_status = 1) AND ((isOrangeMember IN (1) AND member_id IN (:orange)) OR (isOrangeMember IN (0) AND member_id IN (:users))))) ORDER BY LOWER(name) ASC " )
    fun fetchParticipantsWithUserOrange(loginUserId: String, users: List<String>, orange: List<String>):List<ParticipantUsers>*/


    /*@Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,name,email,user_avatar as userpic,user_status,is_orange_member as isOrangeMember,cast(user_id as char) as member_id,user_id FROM user WHERE user_id!=:workspaceUserId AND user_status=1 AND workspace_id = :workspaceId GROUP BY user_id HAVING (((user_status = 1) AND ((isOrangeMember IN (1)) OR (isOrangeMember IN (0) AND member_id IN (:users))))) ORDER BY LOWER(name) ASC " )
    fun fetchWorkspaceParticipantsWithUser(workspaceId: String, workspaceUserId: String, users: List<String>):List<ParticipantUsers>
    @Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,name,email,user_avatar as userpic,user_status,is_orange_member as isOrangeMember,cast(user_id as char) as member_id,user_id FROM user WHERE user_id!=:loginUserId AND user_status=1 GROUP BY user_id HAVING (((user_status = 1) AND ((isOrangeMember IN (1)) OR (isOrangeMember IN (0) AND member_id IN (:users))))) ORDER BY LOWER(name) ASC " )
    fun fetchParticipantsWithUser(loginUserId: String, users: List<String>):List<ParticipantUsers>*/


    /*@Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,name,email,user_avatar as userpic,user_status,is_orange_member as isOrangeMember,cast(user_id as char) as member_id,user_id FROM user WHERE user_id!=:workspaceUserId AND user_status=1 AND workspace_id = :workspaceId GROUP BY user_id HAVING (((user_status = 1) AND ((isOrangeMember IN (1) AND member_id IN (:orange)) OR (isOrangeMember IN (0))))) ORDER BY LOWER(name) ASC " )
    fun fetchWorkspaceParticipantsWithOrange(workspaceId: String, workspaceUserId: String,  orange: List<String>):List<ParticipantUsers>
    @Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,name,email,user_avatar as userpic,user_status,is_orange_member as isOrangeMember,cast(user_id as char) as member_id,user_id FROM user WHERE user_id!=:loginUserId AND user_status=1 GROUP BY user_id HAVING (((user_status = 1) AND ((isOrangeMember IN (1) AND member_id IN (:orange)) OR (isOrangeMember IN (0))))) ORDER BY LOWER(name) ASC " )
    fun fetchParticipantsWithOrange(loginUserId: String,  orange: List<String>):List<ParticipantUsers>*/


    /*@Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,name,email,user_avatar as userpic,user_status,is_orange_member as isOrangeMember,user_id FROM user WHERE user_id!=:workspaceUserId AND user_status=1 AND workspace_id = :workspaceId GROUP BY user_id  ORDER BY LOWER(name) ASC " )
    fun fetchWorkspaceParticipants(workspaceId: String, workspaceUserId: String):List<ParticipantUsers>
    @Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,1 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,name,email,user_avatar as userpic,user_status,is_orange_member as isOrangeMember,user_id FROM user WHERE user_id!=:loginUserId AND user_status=1  GROUP BY user_id  ORDER BY LOWER(name) ASC " )
    fun fetchParticipants(loginUserId: String):List<ParticipantUsers>*/


    /*@Query("SELECT ID,user_id as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,0 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,is_muted as muted,is_favourite as favourite,0 as entity_type,0 as message_status,is_orange_member as orange,user_status as userstatus,0 as group_type,null as message,null as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE workspace_id =:workspaceId AND user_status=0 GROUP BY receiver  ORDER BY lower(entity_name) ASC")
    fun fetchWorkspaceDeactivatedUserList(workspaceId: String): List<RecentList>
    @Query("SELECT ID,user_id as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,0 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,is_muted as muted,is_favourite as favourite,0 as entity_type,0 as message_status,is_orange_member as orange,user_status as userstatus,0 as group_type,null as message,null as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_status=0 GROUP BY receiver  ORDER BY lower(entity_name) ASC")
    fun fetchDeactivatedUserList(): List<RecentList>*/
    /*@Query("SELECT ID,user_id as receiver,0 as message_id,0 as sender_id,user_id as receiver_id,0 as is_read,0 as is_delivered,0 as is_sync,0 as message_type,0 as unread_messages,0 as unread_read_receipt,0 as respond_later_count,0 as muted,0 as favourite,0 as entity_type,0 as message_status,0 as orange,user_status as userstatus,0 as group_type,null as message,null as created_at,name as entity_name,name as searchString,user_avatar as entity_avatar,name as sender_name,'' as workspace_id,null as attachment,6 as onlinestatus,0 as onCallStatus,0 as isBurnoutUser,0 as isTyping,null as typingText,null as normalized_attachment,null as edited_filename,0 as isYouTubeUrl FROM user WHERE user_status=0 GROUP BY receiver  ORDER BY lower(entity_name) ASC")
    fun fetchDeactivatedUserList(): List<RecentList>*/

    /*@Query("DELETE FROM user WHERE workspace_id =:workspaceId")
    fun removeWorkspaceUsers(workspaceId: String)*/
    @Query("DELETE FROM user ")
    fun removeUsers()

    /*@Query("SELECT 0 as screenShareActive,0 as jointlyCodeActive,0 as confStatus,0 as clickable,'' as callConnectionStatus,0 as isPin,0 as callStatus,0 as videoAdded,0 as audioAdded,0 as isVideoMuted,0 as streamType,0 as isAudioMuted,0 as meetingUsetStatus,0 as isHost,0 as isExternalUser,0 as isSelected,name,email,user_avatar as userpic,user_status,is_orange_member as isOrangeMember,user_id FROM user WHERE user_id = :userId  AND user_status=1 AND workspace_id = :workspaceId GROUP BY user_id ")
    fun fetchWorkspaceParticipantUser(userId: String,workspaceId: String):ParticipantUsers*/


    /*@Query("SELECT 0 as clickable, 1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_status =1  and workspace_id = :workspaceId  GROUP BY entity_id HAVING ((orange IN (1) AND entity_id IN (:orange)) OR (orange IN (0) AND entity_id IN (:users) )) order by lower (name) ASC")
    fun fetchWorkspaceMentionAllUsers(workspaceId: String,users:List<String>,orange:List<String>):List<ContactModel>
    @Query("SELECT 0 as clickable, 1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_status =1  GROUP BY entity_id HAVING ((orange IN (1) AND entity_id IN (:orange)) OR (orange IN (0) AND entity_id IN (:users) )) order by lower (name) ASC")
    fun fetchMentionAllUsers(users:List<String>,orange:List<String>):List<ContactModel>*/


    /*@Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_status =1  and workspace_id = :workspaceId  GROUP BY entity_id HAVING ((orange IN (1)) OR (orange IN (0) AND entity_id IN (:users) )) order by lower (name) ASC")
    fun fetchWorkspaceMentionAllUsersWithUsers(workspaceId: String,users:List<String>):List<ContactModel>
    @Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_status =1   GROUP BY entity_id HAVING ((orange IN (1)) OR (orange IN (0) AND entity_id IN (:users) )) order by lower (name) ASC")
    fun fetchMentionAllUsersWithUsers(users:List<String>):List<ContactModel>*/


    /*@Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_status =1  and workspace_id = :workspaceId  GROUP BY entity_id HAVING ((orange IN (1) AND entity_id IN (:orange))  OR (orange IN (0)  )) order by lower (name) ASC")
    fun fetchWorkspaceMentionAllUsersWithOrange(workspaceId: String,orange:List<String>):List<ContactModel>
    @Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_status =1   GROUP BY entity_id HAVING ((orange IN (1) AND entity_id IN (:orange))  OR (orange IN (0)  )) order by lower (name) ASC")
    fun fetchMentionAllUsersWithOrange(orange:List<String>):List<ContactModel>*/


    /*@Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_status =1  and workspace_id = :workspaceId  GROUP BY entity_id HAVING ((orange IN (1) ) OR (orange IN (0) )) order by lower (name) ASC")
    fun fetchWorkspaceMentionAllContacts(workspaceId: String):List<ContactModel>
    @Query("SELECT 0 as clickable,1 as entity_type,cast(user_id as char) as entity_id,user_role,name,email,mobile,is_favourite,is_muted,user_avatar,is_orange_member as orange,workspace_id,0 as isSelected,0 as isAdminClicked,0 as isModeratorClicked from user where user_status =1  GROUP BY entity_id HAVING ((orange IN (1) ) OR (orange IN (0) )) order by lower (name) ASC")
    fun fetchMentionAllContacts():List<ContactModel>*/


    @Query("SELECT * from user WHERE mobile =:mobileNumber")
    fun checkMobileNumberMatch(mobileNumber :String):User

    /*@Query("DELETE FROM user WHERE ID=:id ")
    fun deleteID(id:Long)*/

    /*@Query("SELECT unread_count FROM user WHERE user_id=:userId AND workspace_id=:workspaceId")
    fun fetchWorkspaceUserUnreadCount(userId: String,workspaceId: String):Int*/
    @Query("SELECT unread_messages_count FROM user WHERE user_id=:userId")
    fun fetchUserUnreadCount(userId: String):Int

    /*@Query("SELECT respond_later_count FROM user WHERE user_id=:userId AND workspace_id=:workspaceId")
    fun fetchWorkspaceRespondLaterCount(userId: String,workspaceId: String):Int
    @Query("SELECT respond_later_count FROM user WHERE user_id=:userId")
    fun fetchRespondLaterCount(userId: String,workspaceId: String):Int*/

    /*@Query("SELECT read_receipt_count FROM user WHERE user_id=:userId AND workspace_id=:workspaceId")
    fun fetchWorkspaceReadReceiptCount(userId: String,workspaceId: String):Int
    @Query("SELECT read_receipt_count FROM user WHERE user_id=:userId")
    fun fetchReadReceiptCount(userId: String):Int*/

    @RawQuery(observedEntities = [User::class])
    fun fetchUserListForGroup(query: SupportSQLiteQuery):LiveData<List<ContactModel>>

    @RawQuery(observedEntities = [User::class])
    fun fetchMentionList(query: SupportSQLiteQuery):List<ContactModel>

    @RawQuery(observedEntities = [User::class])
    fun fetchDeactivateList(query: SupportSQLiteQuery):List<RecentList>

    /*@Query("SELECT workspace_id FROM user WHERE workspace_id=:workspaceId")
    fun checkUseWorkspaceDataExists(workspaceId: String):String*/

    @Query("SELECT IFNULL(profile_pic,'') as user_avatar  FROM user WHERE mobile=:phone")
    fun fetchContactAvatar(phone:String): String

    @Query("DELETE FROM user WHERE user_id IN (:userIds)")
    fun deleteUsers(userIds:List<String>)

    @Query("SELECT uid FROM user WHERE user_id IN (:userIds)")
    fun fetchuIds(userIds: List<String>):List<String>

    @Query("SELECT user_id FROM user WHERE is_blocked=1 or blocked_me=1")
    fun fetchBlockedUserIds():List<Long>

    @Query("SELECT blocked_me FROM user WHERE user_id =:userId")
    fun checkIsUserIsBlockedMe(userId: String):Int

    @Query("SELECT is_blocked FROM user WHERE user_id =:userId")
    fun checkIsUserIsBlocked(userId: String):Int

    @Query("SELECT IFNULL (c.ID,0) as ID FROM user as u LEFT JOIN contact as c ON c.uid=u.uid WHERE u.user_id =:userId")
    fun isContact(userId: String):Long

    @Query("SELECT * FROM user WHERE uid IN (:uid)")
    fun fetchUidCallRecord(uid: String) :User

    @Query("SELECT profile_pic FROM user WHERE user_id=:userId")
    fun fetchUserAvatar(userId: String):String

    @Query("SELECT user_id FROM user WHERE user_id IN (:userIds)")
    fun fetchNonUsers(userIds: List<Long>):List<Long>

    @Query("SELECT message_id FROM chat WHERE receiver_id=:receiverId AND sender_id=:senderId AND is_group=0 ORDER BY message_id DESC limit 1")
    fun fetchUserLastMessageId(receiverId:String,senderId:String) :Long

    @Query("SELECT 0 as isSelected,1 as isAlreadyUser,user_id as entity_id,uid,1 as entity,name as entity_name,mobile,country_code,profile_pic entity_avatar,about FROM user WHERE uid =:uid")
    fun fetchProfileByUid(uid: String):UserClientModel

    @Query("SELECT 0 as isSelected,1 as isAlreadyUser,user_id as entity_id,uid,1 as entity,name as entity_name,mobile,country_code,profile_pic entity_avatar,about FROM user WHERE user_id =:userId")
    fun fetchUserProfile(userId: String):UserClientModel

    @Query("SELECT 0 as isSelected,1 as isAlreadyUser,user_id as entity_id,name,uid,1 as entity,name as entity_name,mobile,country_code,profile_pic as entity_avatar FROM user WHERE user_id!=:userId AND user_id NOT IN  (SELECT user_id FROM group_member WHERE group_id=:groupId AND user_status=1) ORDER BY name ASC")
    fun fetchNonGroupUserList(userId: String,groupId:String):List<UserClientModel>

    @Query("SELECT 0 as isSelected,1 as isAlreadyUser,user_id as entity_id,uid,1 as entity,name as entity_name,mobile,country_code,profile_pic as entity_avatar,about FROM user WHERE user_id!=:userId ORDER BY name ASC")
    fun fetchUsersListForCreatingGroup(userId: String):List<UserClientModel>

    @Query("SELECT uid FROM user WHERE user_id!=:userId")
    fun fetchUid(userId: String):String

    @Query("SELECT uid FROM user WHERE user_id IN (:userId)")
    fun fetchUidByUserIds(userId: List<String>):List<String>

    @Query("SELECT name FROM user WHERE user_id=:userId")
    fun fetchUserName(userId: String):String

    @Query("SELECT count(*) as count FROM user WHERE uid =:uid")
    fun isUidExists(uid: String):Int

    @Query("SELECT count(*) as count FROM user WHERE user_id =:userId")
    fun isUserExists(userId: String):Int

    @Query("UPDATE user SET unread_messages_count= (SELECT count(*) as unread_count FROM chat WHERE (sender_id=:senderId AND receiver_id =:receiverId AND is_group = 0 AND is_read = 0)) WHERE user_id=:senderId")
    fun updateUserUnreadCount(senderId: String,receiverId: String)

    //@Query("UPDATE user SET last_message_local_id=:lastMessageLocalId,last_message_id=:lastMessageId,last_message=:lastMessage,last_message_time=:lastMessageTime,last_message_sender_name=:senderName,last_message_sender_id=:senderId,last_message_type=:lastMessageType,last_message_status=(SELECT IFNULL(CASE WHEN last_message_status >:lastMessageStatus THEN last_message_status ELSE :lastMessageStatus END, :lastMessageStatus) as last_message_status FROM user WHERE user_id =:entityId),last_message_by_me =:lastMessageByMe,unread_messages_count =(SELECT count(*) as unread_count FROM chat WHERE (sender_id=:entityId AND receiver_id=:userId AND is_group=0 AND is_read=0)) WHERE user_id =:entityId")
    @Query("UPDATE user SET last_message_local_id=:lastMessageLocalId,last_message_id=:lastMessageId,last_message=:lastMessage,last_message_time=:lastMessageTime,last_message_sender_name=:senderName,last_message_sender_id=:senderId,last_message_type=:lastMessageType,last_message_status=:lastMessageStatus,last_message_by_me =:lastMessageByMe,unread_messages_count =(SELECT count(*) as unread_count FROM chat WHERE (sender_id=:entityId AND receiver_id=:userId AND is_group=0 AND is_read=0)) WHERE user_id =:entityId")
    fun updateUserRecentMessage(lastMessageLocalId:Long,lastMessageId:Long,lastMessage:String,lastMessageTime:String,lastMessageType:Int,lastMessageStatus:Int,entityId: String,userId: String,lastMessageByMe:Int,senderName:String,senderId: String)

    @Query("SELECT * FROM chat WHERE (sender_id=:senderId AND receiver_id=:receiverId AND is_group= 0 AND status!=0) or (sender_id=:receiverId AND receiver_id=:senderId AND is_group=0 AND status!=0) ORDER BY created_at DESC,message_id DESC LIMIT 1")
    fun fetchDataForRecentRecord(senderId: String,receiverId: String):Messenger

    @Query("SELECT is_archived FROM user WHERE user_id=:userId AND is_archived=1")
    fun checkIsArchived(userId: String):Int

    @Query("SELECT 0 as isSelected,1 as isAlreadyUser,user_id as entity_id,uid,1 as entity,name as entity_name,profile_pic as entity_avatar,mobile,country_code,about  FROM user WHERE last_message_local_id=0 AND is_archived = 0 ORDER BY name ASC")
    fun fetchContacts():List<UserClientModel>

    @Query("UPDATE user SET is_archived=1 WHERE user_id IN (:userId)")
    fun updateUserArchived(userId: List<Long>)

    @Query("UPDATE user SET is_archived=0 WHERE user_id IN (:userId)")
    fun updateUserUnArchived(userId: List<Long>)

    @Query("UPDATE user SET is_blocked =:status WHERE user_id IN (:userIds)")
    fun updateUserBlockedStatus(status: Int,userIds: List<Long>)

    @Query("UPDATE user SET blocked_me =:status WHERE user_id IN (:userIds)")
    fun updateUserBlockedMeStatus(status: Int,userIds: List<Long>)

    @Query("UPDATE user SET last_message_local_id=0,last_message_id=0,last_message='',last_message_time='',last_message_type=-1,last_message_status=-1,last_message_by_me=0,unread_messages_count=0 WHERE user_id=:userId")
    fun updateUserLastMessageData(userId: String)

    @Query("UPDATE user SET about=:about,updated_at=:updatedAt WHERE user_id=:userId")
    fun updateUserProfileAbout(about:String,updatedAt: String,userId: String)

    @Query("UPDATE user SET name=:name WHERE user_id=:userId")
    fun updateUserName(userId: String,name: String)

    @RawQuery(observedEntities = [User::class])
    fun updateUserLastMessageStatus(query: SupportSQLiteQuery):Long

    /*@Query("UPDATE user SET last_message_status=:status,updated_at=:updatedAt WHERE user_id=:userId AND (last_message_id =:lastMessageId OR last_message_local_id=:lastMessageLocalId) AND last_message_status<:status")
    fun updateUserLastMessageStatus(status: Int,updatedAt: String,userId: String,lastMessageId: Long,lastMessageLocalId: Long)*/
}