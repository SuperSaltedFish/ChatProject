package com.yzx.chat.module.contact.presenter;

import com.yzx.chat.core.entity.ContactEntity;
import com.yzx.chat.module.contact.contract.RemarkInfoContract;
import com.yzx.chat.core.AppClient;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by YZX on 2018年01月24日.
 * 优秀的代码是它自己最好的文档,当你考虑要添加一个注释时,问问自己:"如何能改进这段代码，以让它不需要注释？"
 */


public class RemarkInfoPresenter implements RemarkInfoContract.Presenter {


    @Override
    public void attachView(RemarkInfoContract.View view) {

    }

    @Override
    public void detachView() {

    }

    @Override
    public void save(ContactEntity contact) {
        AppClient.getInstance().getContactManager().updateContactRemark(contact, null);
    }

    @Override
    public ArrayList<String> getAllTags() {
        Set<String> tags = AppClient.getInstance().getContactManager().getAllTags();
        if (tags != null && tags.size() > 0) {
            return new ArrayList<>(tags);
        } else {
            return null;
        }
    }


}
