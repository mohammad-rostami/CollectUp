package com.collect_up.c_up.model;

import java.util.List;

/**
 * Created by collect-up3 on 10/30/2016.
 */

public class NewGroup {
  private String Title;

  public List<String> getMembersId() {
    return MembersId;
  }

  public void setMembersId(List<String> membersId) {
    MembersId = membersId;
  }

  public String getTitle() {
    return Title;
  }

  public void setTitle(String title) {
    Title = title;
  }

  private List<String> MembersId;
}
