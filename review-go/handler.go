package reviewgo

type Address struct{ Region string }

type Customer struct{ Address *Address }

func Region(customer Customer) string {
	return customer.Address.Region
}
