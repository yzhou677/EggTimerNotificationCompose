import com.example.android.eggtimernotificationcompose.di.Logger
import com.example.android.eggtimernotificationcompose.manager.FireBaseManager
import com.example.android.eggtimernotificationcompose.model.Product
import com.example.android.eggtimernotificationcompose.model.Recipe
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue


class FireBaseManagerTest {

    private lateinit var fireBaseManager: FireBaseManager
    private lateinit var querySnapshot: QuerySnapshot
    private lateinit var task: Task<QuerySnapshot>
    private lateinit var collectionReference: CollectionReference
    private lateinit var query: Query
    private val db: FirebaseFirestore = mock()
    private val mockLogger: Logger = mock()

    @Before
    fun setup() {
        fireBaseManager = FireBaseManager(db, mockLogger)
        querySnapshot = mock()
        task = mock()
        collectionReference = mock()
        query = mock()

        whenever(task.isSuccessful).thenReturn(true)
        whenever(task.result).thenReturn(querySnapshot)
        whenever(task.addOnSuccessListener(any())).thenAnswer {
            val listener = it.getArgument<OnSuccessListener<QuerySnapshot>>(0)
            listener.onSuccess(querySnapshot)
            task
        }
        whenever(task.addOnFailureListener(any())).thenAnswer {
            val listener = it.getArgument<OnFailureListener>(0)
            listener.onFailure(RuntimeException("Test exception"))
            task
        }

        whenever(query.get()).thenReturn(task)

    }

    @Test
    fun `test loadSortedProducts`() {
        val document1: DocumentSnapshot = mockDocumentSnapshot(10.0)
        val document2: DocumentSnapshot = mockDocumentSnapshot(20.0)
        whenever(querySnapshot.documents).thenReturn(listOf(document1, document2))

        // Mock the query and collection
        whenever(db.collection("products")).thenReturn(collectionReference)
        whenever(collectionReference.orderBy("price", Query.Direction.ASCENDING)).thenReturn(query)

        // Act
        val products = mutableListOf<Product>()
        fireBaseManager.loadSortedProducts({ result ->
            products.addAll(result)
        }, true)

        // Assert
        assertTrue(products.isNotEmpty())
        assertEquals("Sample Name", products[0].name)
        assertEquals(10.0, products[0].price, 0.0)
        assertEquals(20.0, products[1].price, 0.0)
        assertEquals("http://example.com/image.png", products[0].imageUrl)
        assertEquals("http://example.com", products[0].link)
    }


    @Test
    fun `test loadRecipes`() {
        val document: DocumentSnapshot = mockRecipeDocumentSnapshot()
        whenever(querySnapshot.documents).thenReturn(listOf(document))

        whenever(db.collection("recipes")).thenReturn(collectionReference)
        whenever(collectionReference.orderBy("name", Query.Direction.ASCENDING)).thenReturn(query)

        // Act
        val recipes = mutableListOf<Recipe>()
        fireBaseManager.loadRecipes({ result ->
            recipes.addAll(result)
        })

        // Assert
        assertTrue(recipes.isNotEmpty())
        assertEquals("Sample Name", recipes[0].name)
        assertEquals(100.0, recipes[0].calories)
        assertEquals("http://example.com/image.png", recipes[0].imageUrl)
        assertEquals("http://example.com", recipes[0].link)
    }

    private fun mockDocumentSnapshot(price: Double): DocumentSnapshot {
        val document: DocumentSnapshot = mock()
        whenever(document.getString("name")).thenReturn("Sample Name")
        whenever(document.getDouble("price")).thenReturn(price)
        whenever(document.getString("imageUrl")).thenReturn("http://example.com/image.png")
        whenever(document.getString("link")).thenReturn("http://example.com")
        return document
    }

    private fun mockRecipeDocumentSnapshot(): DocumentSnapshot {
        val document: DocumentSnapshot = mock()
        whenever(document.getString("name")).thenReturn("Sample Name")
        whenever(document.getDouble("calories")).thenReturn(100.0)
        whenever(document.getString("imageUrl")).thenReturn("http://example.com/image.png")
        whenever(document.getString("link")).thenReturn("http://example.com")
        return document
    }
}
