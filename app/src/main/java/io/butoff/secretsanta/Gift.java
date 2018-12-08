package io.butoff.secretsanta;

import com.google.gson.annotations.SerializedName;

public final class Gift {

    @SerializedName("name")
    public String providerName;

    @SerializedName("mail")
    public String providerEmail;

    @SerializedName("name_to")
    public String consumerName;

    @SerializedName("mail_to")
    public String consumerEmail;
}
