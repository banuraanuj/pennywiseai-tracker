import Shared
import SwiftUI

struct AddEditAccountScreen: View {
    @ObservedObject var viewModel: AccountsViewModel
    var editBankName: String?
    var editAccountLast4: String?
    var editAccount: SharedAccountItem?
    var onSave: () -> Void
    @Environment(\.dismiss) private var dismiss

    @State private var bankName = ""
    @State private var originalBankName = ""
    @State private var accountLast4 = ""
    @State private var accountType = "SAVINGS"
    @State private var balanceText = ""
    @State private var currency = CurrencyManager.shared.displayCurrency

    private let accountTypes = ["SAVINGS", "CURRENT", "CASH", "FIXED_DEPOSIT", "RECURRING_DEPOSIT", "CREDIT"]
    private let currencies = CurrencyPickerScreen.currencies.map(\.code)

    private var isEditing: Bool { editAccount != nil }

    private func formatAccountType(_ type: String) -> String {
        switch type {
        case "FIXED_DEPOSIT": return "Fixed Deposit"
        case "RECURRING_DEPOSIT": return "Recurring Deposit"
        default: return type.capitalized
        }
    }

    var body: some View {
        Form {
            Section("Account Details") {
                TextField("Bank Name", text: $bankName)
                    .autocorrectionDisabled()

                TextField("Last 4 Digits", text: $accountLast4)
                    .keyboardType(.numberPad)
                    .disabled(isEditing)
                    .onChange(of: accountLast4) { newValue in
                        let filtered = String(newValue.filter(\.isNumber).prefix(4))
                        if filtered != newValue { accountLast4 = filtered }
                    }

                Picker("Account Type", selection: $accountType) {
                    ForEach(accountTypes, id: \.self) { type in
                        Text(formatAccountType(type)).tag(type)
                    }
                }
            }

            Section("Balance") {
                TextField("Initial Balance", text: $balanceText)
                    .keyboardType(.decimalPad)

                Picker("Currency", selection: $currency) {
                    ForEach(currencies, id: \.self) { curr in
                        Text(curr).tag(curr)
                    }
                }
            }
        }
        .navigationTitle(isEditing ? "Edit Account" : "Add Account")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Cancel") { dismiss() }
            }
            ToolbarItem(placement: .confirmationAction) {
                Button("Save") {
                    saveAccount()
                }
                .disabled(!isFormValid)
            }
        }
        .onAppear {
            if let account = editAccount {
                bankName = account.bankName
                originalBankName = account.bankName
                accountLast4 = account.accountLast4
                accountType = account.accountType ?? "SAVINGS"
                currency = account.currency
                let whole = account.balanceMinor / 100
                let fraction = abs(account.balanceMinor) % 100
                balanceText = "\(whole).\(String(format: "%02d", Int(fraction)))"
            }
        }
    }

    private var isFormValid: Bool {
        !bankName.trimmingCharacters(in: .whitespaces).isEmpty &&
        accountLast4.count == 4
    }

    private func saveAccount() {
        let trimmedName = bankName.trimmingCharacters(in: .whitespaces)
        let balanceMinor = parseBalanceMinor(balanceText)
        if isEditing && trimmedName != originalBankName {
            _ = viewModel.renameAccount(
                oldBankName: originalBankName,
                newBankName: trimmedName,
                accountLast4: accountLast4
            )
        }
        _ = viewModel.createAccount(
            bankName: trimmedName,
            accountLast4: accountLast4,
            accountType: accountType,
            balanceMinor: balanceMinor,
            currency: currency
        )
        onSave()
        dismiss()
    }

    private func parseBalanceMinor(_ text: String) -> Int64 {
        guard let value = Double(text) else { return 0 }
        return Int64(value * 100)
    }
}
