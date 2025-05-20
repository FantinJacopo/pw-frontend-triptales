package com.triptales.app.data.post

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * API per la gestione dei post.
 * Include endpoint per ottenere i post di un gruppo e creare nuovi post con supporto per analisi ML Kit.
 */
interface PostApi {
    /**
     * Ottiene tutti i post di un gruppo specifico.
     *
     * @param groupId ID del gruppo
     * @return Lista di post del gruppo
     */
    @GET("groups/{group_id}/posts/")
    suspend fun getPosts(@Path("group_id") groupId: Int): Response<List<Post>>

    /**
     * Crea un nuovo post con supporto per geolocalizzazione e analisi ML Kit.
     *
     * NOTA: Verifica che i nomi dei parametri corrispondano esattamente ai nomi dei campi
     * attesi dal backend Django. Per esempio, 'ocr_text' e 'object_tags' devono corrispondere
     * ai nomi dei campi nel modello Post del backend.
     *
     * @param image Immagine del post
     * @param tripGroup ID del gruppo a cui appartiene il post
     * @param smartCaption Didascalia del post
     * @param latitude Latitudine (opzionale)
     * @param longitude Longitudine (opzionale)
     * @param ocrText Testo estratto dall'immagine tramite OCR (opzionale)
     * @param objectTags Tag degli oggetti rilevati nell'immagine (opzionale)
     * @return Il post creato
     */
    @Multipart
    @POST("posts/")
    suspend fun createPost(
        @Part image: MultipartBody.Part,
        @Part("trip_group") tripGroup: RequestBody,
        @Part("smart_caption") smartCaption: RequestBody,
        @Part("latitude") latitude: RequestBody? = null,
        @Part("longitude") longitude: RequestBody? = null,
        @Part("ocr_text") ocrText: RequestBody? = null,
        @Part("object_tags") objectTags: RequestBody? = null
    ): Response<Post>
}