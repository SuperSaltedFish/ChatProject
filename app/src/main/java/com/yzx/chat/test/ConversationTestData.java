package com.yzx.chat.test;

import com.yzx.chat.bean.ConversationBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YZX on 2017年06月17日.
 * 生命太短暂,不要去做一些根本没有人想要的东西
 */

public class ConversationTestData {

    public static List<ConversationBean> getTestData() {
        List<ConversationBean> conversationList = new ArrayList<>();
        ConversationBean.Single single1 = new ConversationBean.Single();
        ConversationBean.Single single2 = new ConversationBean.Single();
        ConversationBean.Single single3 = new ConversationBean.Single();
        ConversationBean.Single single4 = new ConversationBean.Single();
        ConversationBean.Single single5 = new ConversationBean.Single();
        ConversationBean.Single single6 = new ConversationBean.Single();
        ConversationBean.Group group1 = new ConversationBean.Group();
        ConversationBean.Group group2 = new ConversationBean.Group();
        single1.setName("Judy Santos");
        single2.setName("Paul Martinez");
        single3.setName("Frank Thompson");
        single4.setName("DonaId Knight");
        single5.setName("Brian Robertson");
        single6.setName("Rachel Owens");
        group1.setName("Group conversation");
        group2.setName("Group conversation");
        single1.setLastRecord("Banjo tote bag bicycle rights ,Hi.");
        single2.setLastRecord("Fixie tite bag ethnic keytar");
        single3.setLastRecord("Tousied food truck polaroid ,saivodiw shawos");
        single4.setLastRecord("Hella narwhal");
        single5.setLastRecord("food truck");
        single6.setLastRecord("Bushwick meh Blue Bottle bell");
        group1.setLastRecord("Retro occupy organic,Stumptown shcdwa");
        group2.setLastRecord("Retro occupy organic,Stumptown shcdwa");
        single1.setTime("14:32");
        single2.setTime("13:12");
        single3.setTime("10:53");
        single4.setTime("09:25");
        single5.setTime("08:38");
        single6.setTime("03:41");
        group1.setTime("01:32");
        group2.setTime("21:01");
        conversationList.add(single1);
        conversationList.add(single2);
        conversationList.add(group1);
        conversationList.add(single3);
        conversationList.add(single4);
        conversationList.add(group2);
        conversationList.add(single5);
        conversationList.add(single6);

        return conversationList;

    }

}
