package najmi.learn.userserviceclient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)// anotasi junit dengan mockito
class UserServiceClientApplicationTests {


	/*
	The most widely used annotation in Mockito is @Mock.
	We can use @Mock to create and inject mocked instances without having to call Mockito.mock manually.
	In the following example, we'll create a mocked ArrayList manually without using the @Mock annotation:
	 */
	@Mock
	List<String> mockedList;

	@Spy
	List<String> spiedList = new ArrayList<>();

	@BeforeEach
	void contextLoads() {
		// alternative buat anotasi mockito
		// MockitoAnnotations.openMocks(this);
	}

	@Test
	public void jajalMock(){
		mockedList.add("waduh");
		Mockito.verify(mockedList).add("waduh");
		Assertions.assertEquals(0, mockedList.size());

		Mockito.when(mockedList.size()).thenReturn(100);
		Assertions.assertEquals(100, mockedList.size());
	}

	@Test
	public void jajalSpy(){
		spiedList.add("one");
		spiedList.add("two");

		Mockito.verify(spiedList).add("one");
		Mockito.verify(spiedList).add("two");

		Assertions.assertEquals(2, spiedList.size());

		Mockito.doReturn(100).when(spiedList).size();
		Assertions.assertEquals(spiedList.size(), 100);
	}
}
