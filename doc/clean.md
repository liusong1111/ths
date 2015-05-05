
    delete from topics where user_id in ();
    delete from replies where user_id in ();
    delete from user_labels where user_id in ();
    delete from invitations where inviter_id in () or invitee_id in ();
    delete from friends where user_id in () or friend_id in ();
    delete from feedbacks where user_id in ();
    delete from isolatelog where user_id in ();
    delete from users where id in ();











