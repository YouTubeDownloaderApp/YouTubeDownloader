@Composable
fun CookiePreferenceScreen(
    onSave: (String) -> Unit,
    onBack: () -> Unit
) {
    var cookieText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("YouTube Cookies") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
        
        Text(
            text = "Paste your YouTube cookies (Netscape format)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        OutlinedTextField(
            value = cookieText,
            onValueChange = { cookieText = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            placeholder = { Text("Example:\n.youtube.com\tTRUE\t/\tTRUE\t1234567890\tVISITOR_INFO1_LIVE\t...") },
            shape = RoundedCornerShape(8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onSave(cookieText) },
            modifier = Modifier.fillMaxWidth(),
            enabled = cookieText.isNotBlank()
        ) {
            Text("Save Cookies")
        }
    }
}
