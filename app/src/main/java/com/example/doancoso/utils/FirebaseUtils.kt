import android.net.Uri
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync

fun createShareableLink(planId: String, onLinkCreated: (String?) -> Unit) {
    val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
        .setLink(Uri.parse("https://doancoso.com/plan?planId=$planId"))
        .setDomainUriPrefix("https://doancoso.page.link")
        .setAndroidParameters(
            DynamicLink.AndroidParameters.Builder()
                .setMinimumVersion(1)
                .build()
        )
        .buildDynamicLink()

    FirebaseDynamicLinks.getInstance().shortLinkAsync {
        longLink = dynamicLink.uri
    }.addOnSuccessListener { result ->
        val shortLink = result.shortLink
        onLinkCreated(shortLink.toString())
    }.addOnFailureListener {
        onLinkCreated(null)
    }
} 