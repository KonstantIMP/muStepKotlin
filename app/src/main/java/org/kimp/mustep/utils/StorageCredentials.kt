package org.kimp.mustep.utils

import com.amazonaws.auth.AWSCredentials
import org.kimp.mustep.BuildConfig

class StorageCredentials : AWSCredentials {
    override fun getAWSAccessKeyId(): String = BuildConfig.AMAZON_AWS_S3_KEI_ID
    override fun getAWSSecretKey(): String = BuildConfig.AMAZON_AWS_S3_ACCESS_KEY
}