package search

object Data {
  val texts = List(
    Book(1, "The Time Machine", "A man travels through time and witnesses the evolution of humanity.", "H.G. Wells", 1895),
    Book(2, "Ender's Game", "A young boy is trained to become a military leader in a war against an alien race.", "Orson Scott Card", 1985),
    Book(3, "Brave New World", "A dystopian society where people are genetically engineered and conditioned to conform to a strict social hierarchy.", "Aldous Huxley", 1932),
    Book(4, "The Hitchhiker's Guide to the Galaxy", "A comedic science fiction series following the misadventures of an unwitting human and his alien friend.", "Douglas Adams", 1979),
    Book(5, "Dune", "A desert planet is the site of political intrigue and power struggles.", "Frank Herbert", 1965),
    Book(6, "Foundation", "A mathematician develops a science to predict the future of humanity and works to save civilization from collapse.", "Isaac Asimov", 1951),
    Book(7, "Snow Crash", "A futuristic world where the internet has evolved into a virtual reality metaverse.", "Neal Stephenson", 1992),
    Book(8, "Neuromancer", "A hacker is hired to pull off a near-impossible hack and gets pulled into a web of intrigue.", "William Gibson", 1984),
    Book(9, "The War of the Worlds", "A Martian invasion of Earth throws humanity into chaos.", "H.G. Wells", 1898),
    Book(10, "The Hunger Games", "A dystopian society where teenagers are forced to fight to the death in a televised spectacle.", "Suzanne Collins", 2008),
    Book(11, "The Andromeda Strain", "A deadly virus from outer space threatens to wipe out humanity.", "Michael Crichton", 1969),
    Book(12, "The Left Hand of Darkness", "A human ambassador is sent to a planet where the inhabitants are genderless and can change gender at will.", "Ursula K. Le Guin", 1969),
    Book(13, "The Three-Body Problem", "Humans encounter an alien civilization that lives in a dying system.", "Liu Cixin", 2008),
    Book(14, "Pride and Prejudice", "A romantic novel that explores the manners, education, marriage, and morality of the British landed gentry at the end of the 18th century.", "Jane Austen", 1813),
    Book(15, "To Kill a Mockingbird", "A gripping story of racial injustice and moral growth set in the Deep South during the 1930s.", "Harper Lee", 1960),
    Book(16, "The Great Gatsby", "An iconic novel that explores themes of wealth, love, and decadence in the Roaring Twenties.", "F. Scott Fitzgerald", 1925),
    Book(17, "Wuthering Heights", "A classic romance about the intense and destructive relationship between Catherine Earnshaw and Heathcliff.", "Emily Bronte", 1847),
    Book(18, "Jane Eyre", "A novel of morality, education, and romance that follows the experiences of its heroine, including her growth to adulthood.", "Charlotte Bronte", 1847),
    Book(19, "Moby Dick", "An epic tale of obsession, adventure, and survival at sea as a sailor hunts down the elusive white whale.", "Herman Melville", 1851),
    Book(20, "War and Peace", "A historical novel that chronicles the lives of five aristocratic families against the backdrop of the French invasion of Russia.", "Leo Tolstoy", 1869),
    Book(21, "Anna Karenina", "A tragic love story about a married woman who falls in love with an army officer, ultimately leading to her downfall.", "Leo Tolstoy", 1870),
    Book(22, "The Catcher in the Rye", "A classic coming-of-age novel, The Catcher in the Rye follows Holden Caulfield as he leaves his prep school and wanders New York City.", "J.D. Salinger", 1951),
    Book(23, "Beloved", "Beloved is a haunting and powerful novel that explores the emotional and psychological impact of slavery on African Americans.", "Toni Morrison", 1987),
    Book(24, "The Alchemist", "The Alchemist is a magical and inspiring tale of self-discovery and personal growth.", "Paulo Coelho", 1988),
    Book(25, "The Road", "A post-apocalyptic novel of survival and hope, The Road follows a man and his young son as they journey through a desolate landscape.", "Cormac McCarthy", 2006),
    Book(26, "The Kite Runner", "The Kite Runner is a powerful and moving story of redemption and forgiveness. Set against the backdrop of Afghanistan, the novel follows Amir, a young boy who must come to terms with his past mistakes and the consequences they have had on those he loves.", "Khaled Hosseini", 2003),
    Book(27, "The Book Thief", "A poignant and thought-provoking novel set during World War II, The Book Thief follows the story of Liesel Meminger, a young girl who steals books to share with her foster family and to keep for herself.", "Markus Zusak", 2005),
    Book(28, "The Nightingale", "Set during World War II in France, The Nightingale tells the story of two sisters, Vianne and Isabelle, who are forced to confront the harsh realities of war and the choices they must make to protect their families and themselves.", "Kristin Hannah", 2015),
    Book(29, "The Girl on the Train", "A psychological thriller that will keep you guessing until the very end, The Girl on the Train follows the story of Rachel Watson, a woman who becomes entangled in a missing persons case when she believes she has seen the victim on her daily commute.", "Paula Hawkins", 2015),
    Book(30, "Les Misérables", "A classic novel of French literature, Les Misérables follows the story of Jean Valjean, a former convict who seeks to redeem himself after serving nineteen years in jail for stealing a loaf of bread.", "Victor Hugo", 1862),
    Book(31, "Madame Bovary", "A scathing critique of provincial life in 19th century France, Madame Bovary tells the story of Emma Bovary, a bored and disillusioned wife who seeks excitement and fulfillment through extramarital affairs and consumerism.", "Gustave Flaubert", 1857),
    Book(32, "Remembrance of Things Past", "A groundbreaking work of modern literature, In Search of Lost Time (or Remembrance of Things Past) is a seven-volume novel that explores the inner world of its narrator, Charles Swann, as he recalls his past experiences and comes to understand their significance in shaping his present self.", "Marcel Proust", 1896),
    Book(33, "The Stranger", "A haunting tale of existentialist themes, The Stranger follows Meursault, a man who commits a senseless murder and feels no remorse or guilt for his actions. As he wanders through the Mediterranean landscape, Meursault grapples with the meaninglessness of life and the absurdity of human existence.", "Albert Camus", 1942)
  )

  final case class Book(id: Int, title: String, description: String, author: String, year: Int)
}
