#include <iostream>


using namespace std;

template <typename T>
class SmartPointer : public exception
{
protected:
	T *ptr;
	bool isArr;		//member of the class that serves to determine if prt points to an array
public:
	//constructors
	SmartPointer<T>();
	SmartPointer<T>(T a);
	SmartPointer<T>(T a[], int l);
	//getters and setters
	T getValue();
	void setValue(T);
	//getters and setters to use exclusively for arrays
	T getValueAt(int i);
	void setValueAt(int i, T val);
	//Operator overloading functions
	friend SmartPointer<T> operator* (SmartPointer<T>& p1, SmartPointer<T>& p2) {
			SmartPointer<T> s;
			*s.ptr = *p1.ptr * *p2.ptr;
			return s;
	}
	friend SmartPointer<T> operator+ <> (SmartPointer<T>&, SmartPointer<T>&);
	friend SmartPointer<T> operator- <> (SmartPointer<T>&, SmartPointer<T>&);
};

//Default constructor. Catches bad alloc exception. Creates an empty obj and array flag is set to false
template <typename T>
SmartPointer<T>::SmartPointer<T>()
{
	try {
		ptr = new T();
		isArr = false;
	}
	catch (const bad_alloc& e)
	{
		cout << "Ran out of memory. Allocation failed: " << e.what() << endl;
	}
}

//Constructor assigns the arg to ptr. Sets array falg to flase. Throws arg < 0 exception and catches bad_alloc
template <typename T>
SmartPointer<T>::SmartPointer<T>(T a)
{
	try {
		if (a < 0)
		{
			throw a;
		}
	} 
	catch (T a) {
		cout << "Invalid argument exception caught. Only postive numbers allowed. Argument=" << a << endl;
	}
	try
	{
		ptr = new T(a);
		isArr = false;
	}
	catch (const bad_alloc& e)
	{
		cout << "Ran out of memory. Allocation failed: " << e.what() << endl;
	}
}

//Constructor for ptr to array. Array is not initialized and arra flag is set to true. Catches bad alloc exception.
template<typename T>
SmartPointer<T>::SmartPointer<T>(T a[], int l)
{
	try {
		ptr = new T[l];
		isArr = true;
	}
	catch (const bad_alloc& e)
	{
		cout << "Ran out of memory. Allocation failed: " << e.what() << endl;
	}
}

//Gets the ptr value. No reference passing.
template <typename T>
T SmartPointer<T>::getValue()
{
	T v = *ptr;
	return v;
}

//Sets ptr value to argument. Catches negative number exception.
template <typename T>
void SmartPointer<T>::setValue(T a)
{
	try {
		if (a < 0)
		{
			throw a;
		}
		else {
			*ptr = a;
		}
	}
	catch (T a) {
		cout << "Invalid argument exception caught. Only postive numbers allowed. Argument=" << a << endl;
	}
}

//Gets value at array index i. No reference passing.
template<typename T>
T SmartPointer<T>::getValueAt(int i)
{
	T v = *(ptr + i);
	return v;
}

//Sets value in array index i to val. Catches negative number exception.
template<typename T>
void SmartPointer<T>::setValueAt(int i, T val)
{
	try {
		if (val < 0)
		{
			throw val;
		}
		else {
			*(ptr + i) = val;
		}
	}
	catch (T val) {
		cout << "Invalid argument exception caught. Only postive numbers allowed. Argument=" << val << endl;
	}
	
}

//operator+ overloading
template <typename T>
SmartPointer<T> operator+ (SmartPointer<T>& p1, SmartPointer<T>& p2)
{
		SmartPointer<T> s;
		*s.ptr = *p1.ptr + *p2.ptr;
		return s;
}

//operator- overloading. Catches negative number exception if the operation results in a negative number.
template <typename T>
SmartPointer<T> operator- (SmartPointer<T>& p1, SmartPointer<T>& p2)
{
	try {
		if (*p1.ptr - *p2.ptr<0)
		{
			throw *p1.ptr - *p2.ptr;
		}
		else {
			SmartPointer<T> s;
			*s.ptr = *p1.ptr - *p2.ptr;
			return s;
		}
	}
	catch (T a) {
		cout << "Invalid arguments, the class does not support negative values.\nNext time make sure the operation does not give a negative number.\nRes: " << a << endl;
	}
	
}

int main()
{
	// For SmartPointer class
	cout << "Testing SmartPointer class" << endl;

	// Testing Constructors
	cout << "Creating a SmartPointer of type int with value 11" << endl;
	SmartPointer<int> SmartIntPointer1(6);
	cout << "SmartIntPointer1 = " << SmartIntPointer1.getValue() << endl;

	cout << "Creating a SmartPointer of type int with value -1" << endl;
	SmartPointer<int> SmartIntPointer(-1);

	cout << "Creating a SmartPointer of type int with no value provided" << endl;
	SmartPointer<int> SmartIntPointer2;
	cout << "SmartIntPointer2 = " << SmartIntPointer2.getValue() << endl;

	// Testing Setter & Getter
	cout << "Setting value of S to -5 with setter. Testing setter exception handling." << endl;
	SmartPointer<int> S;
	S.setValue(-5);
	
	cout << "Setting value of SmartIntPointer2 to 5" << endl;
	SmartIntPointer2.setValue(5);
	cout << "SmartIntPointer2 = " << SmartIntPointer2.getValue() << endl;

	cout << "Creating a SmartPointer of type float with no value provided" << endl;
	SmartPointer<float> SmartFloatPointer1;
	cout << "SmartFloatPointer1 = " << SmartFloatPointer1.getValue() << endl;

	cout << "Setting value of SmartFloatPointer1 to 1.5" << endl;
	SmartFloatPointer1.setValue(1.5);
	cout << "SmartFloatPointer1 = " << SmartFloatPointer1.getValue() << endl;

	cout << "Creating a SmartPointer of type float with no value provided" << endl;
	SmartPointer<float> SmartFloatPointer2;
	cout << "SmartFloatPointer2 = " << SmartFloatPointer2.getValue() << endl;

	cout << "Setting value of SmartFloatPointer2 to 2.5" << endl;
	SmartFloatPointer2.setValue(2.5);
	cout << "SmartFloatPointer2 = " << SmartFloatPointer2.getValue() << endl;

	SmartPointer<float> SmartFloatPointer3 = SmartFloatPointer1 + SmartFloatPointer2;
	cout << "SmartFloatPointer1 + SmartFloatPointer2 = " << SmartFloatPointer3.getValue() << endl;
	
	cout << "Testing exception handling for negative result coming from - operator overload." << endl;
	SmartPointer<double> s2(5), s3(9),s1 = s2 - s3;
	
	SmartPointer<float> SmartFloatPointer4 = SmartFloatPointer2 - SmartFloatPointer1;
	cout << "SmartFloatPointer2 - SmartFloatPointer1 = " << SmartFloatPointer4.getValue() << endl;

	SmartPointer<float> SmartFloatPointer5 = SmartFloatPointer1 * SmartFloatPointer2;
	cout << "SmartFloatPointer1 * SmartFloatPointer2 = " << SmartFloatPointer5.getValue() << endl;

	// For handling arrays
	cout << "Testing arrays" << endl;

	// 
	// add the needed code that shows how you use your class to create an array of multiple elements of a certain type.
	// provide all the necessary test code that shows the different use cases of your code

	//Declare normal arrays
	double a[2];
	int b[2];

	//To initialize a dynamic array pass a created array as 1st argument. Second arguemnt is the length previously defined for the array. 
	SmartPointer<double> arr(a, 2);
	SmartPointer<int> arr1(b, 2);

	//Testing setters
	arr.setValueAt(0, 1.2);
	arr.setValueAt(1, 2.2);

	arr1.setValueAt(0, 8);
	arr1.setValueAt(1, 10);

	cout << "Setting value of arr[0] to -5 with setter. Testing setter exception handling." << endl;
	arr.setValueAt(0, -5);

	//Testing getters
	cout << "arr[0] = " << arr.getValueAt(0) << endl;
	cout << "arr[1] = " << arr.getValueAt(1) << endl;
	cout << "arr1[0] = " << arr1.getValueAt(0) << endl;
	cout << "arr1[1] = " << arr1.getValueAt(1) << endl;

	/*
	I couldn't overload the operators for the array scenario. That's the only thing I couldn't implement without getting
	tons of stack erros and the like. So I rolled back all the changes so that this code would compile and run withour raising exceptions.

	Underneath is answer to Q1
	*/

	/*
	Q1: Since c++ 11 there are 3 types of smart pointers: unique_ptr, shared_ptr and weak_ptr.
	A unique_ptr own a dynamically allocated object. It makes sure there is only one copy of said ressource and thus it does not support 
	copy mechanisms, however it does support move. Finally, when time comes a unique_ptr will delete it's object, without caring if other pointers are
	still referring to it.
	Unlike a unique_ptr, several shared_ptr may own the same object. An internal counter keeps reference to how many shared_ptr there are for a given obj.
	A useful scenario is when we want to pass a reference beyond the scope of a function. The object is destroyed when either the last shared_ptr is destroyed 
	or when it is assigned another ptr.
	A weak_ptr is related to a shared_ptr. Unlike the latter it holds a non-owning reference to an object, mainly used to keep track of an obj. It can be
	converted to shared_ptr. If an object is deleted while a converted weak_ptr refers to it the obj lifetime will be extended. Commonly used
	to break references cycles formed by obj managed by shared_ptr's

	sources: 
	https://www.internalpointers.com/post/beginner-s-look-smart-pointers-modern-c
	https://en.cppreference.com/book/intro/smart_pointers
	https://en.cppreference.com/book/intro/smart_pointers
	https://en.cppreference.com/w/cpp/memory/shared_ptr
	https://en.cppreference.com/w/cpp/memory/weak_ptr
	*/
}
